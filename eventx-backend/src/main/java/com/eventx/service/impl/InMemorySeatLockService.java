package com.eventx.service.impl;

import com.eventx.service.SeatLockService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Fallback when Redis is down. Locks are per-process only. */
@Slf4j
class InMemorySeatLockService implements SeatLockService {

    private static class LockEntry {
        String value;
        long expiryTimeMs;

        LockEntry(String value, long expiryTimeMs) {
            this.value = value;
            this.expiryTimeMs = expiryTimeMs;
        }
    }

    private final int lockTtlSeconds;
    private final Map<String, LockEntry> locks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "seat-lock-cleanup");
        t.setDaemon(true);
        return t;
    });

    InMemorySeatLockService(int lockTtlSeconds) {
        this.lockTtlSeconds = lockTtlSeconds;
        cleanupExecutor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            locks.entrySet().removeIf(entry -> entry.getValue().expiryTimeMs < now);
        }, 1, 1, TimeUnit.MINUTES);
    }

    static String key(Long eventId, Long seatId) {
        return "eventx:seat:" + eventId + ":" + seatId;
    }

    @Override
    public boolean lockSeat(Long eventId, Long seatId, Long userId, Long bookingId) {
        String key = key(eventId, seatId);
        String value = "user:" + userId + ":booking:" + bookingId;
        long expiryTimeMs = System.currentTimeMillis() + (lockTtlSeconds * 1000L);

        LockEntry currentLock = locks.get(key);
        if (currentLock != null && currentLock.expiryTimeMs < System.currentTimeMillis()) {
            locks.remove(key);
        }

        LockEntry newLock = new LockEntry(value, expiryTimeMs);
        LockEntry previousLock = locks.putIfAbsent(key, newLock);

        if (previousLock == null || previousLock.expiryTimeMs < System.currentTimeMillis()) {
            if (previousLock != null) {
                locks.put(key, newLock);
            }
            return true;
        }
        log.warn("In-memory lock held for seat {} on event {}", seatId, eventId);
        return false;
    }

    @Override
    public void unlockSeat(Long eventId, Long seatId) {
        locks.remove(key(eventId, seatId));
    }

    @Override
    public void unlockSeatsForBooking(Long eventId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) return;
        for (Long seatId : seatIds) {
            unlockSeat(eventId, seatId);
        }
    }

    @Override
    public boolean isSeatLocked(Long eventId, Long seatId) {
        LockEntry lock = locks.get(key(eventId, seatId));
        if (lock == null) return false;
        if (lock.expiryTimeMs < System.currentTimeMillis()) {
            locks.remove(key(eventId, seatId));
            return false;
        }
        return true;
    }

    @Override
    public String getLockHolder(Long eventId, Long seatId) {
        LockEntry lock = locks.get(key(eventId, seatId));
        if (lock != null && lock.expiryTimeMs >= System.currentTimeMillis()) {
            return lock.value;
        }
        return null;
    }

    @Override
    public long getLockTTL(Long eventId, Long seatId) {
        LockEntry lock = locks.get(key(eventId, seatId));
        if (lock != null) {
            long remainingMs = lock.expiryTimeMs - System.currentTimeMillis();
            return remainingMs > 0 ? remainingMs / 1000 : 0L;
        }
        return 0L;
    }
}
