package com.eventx.service.impl;

import com.eventx.service.SeatLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SeatLockServiceImpl implements SeatLockService {

    private final StringRedisTemplate redis;
    private final InMemorySeatLockService memory;
    private final int lockTtlSeconds;
    private volatile long redisDownUntilMs;

    public SeatLockServiceImpl(
            @Autowired(required = false) StringRedisTemplate redis,
            @Value("${app.seat-lock.ttl-seconds:300}") int lockTtlSeconds) {
        this.redis = redis;
        this.lockTtlSeconds = lockTtlSeconds;
        this.memory = new InMemorySeatLockService(lockTtlSeconds);
        if (redis == null) {
            log.warn("Redis is not configured. Seat locks will stay in process memory.");
        } else {
            log.info("Seat locks use Redis SET NX with TTL {}s", lockTtlSeconds);
        }
    }

    private boolean redisReady() {
        return redis != null && System.currentTimeMillis() >= redisDownUntilMs;
    }

    private void markRedisDown(Exception ex) {
        redisDownUntilMs = System.currentTimeMillis() + 10_000L;
        log.warn("Redis unavailable ({}), using in-memory seat locks for 10s", ex.getMessage());
    }

    @Override
    public boolean lockSeat(Long eventId, Long seatId, Long userId, Long bookingId) {
        String key = InMemorySeatLockService.key(eventId, seatId);
        String value = "user:" + userId + ":booking:" + bookingId;
        try {
            if (redisReady()) {
                Boolean acquired = redis.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(lockTtlSeconds));
                if (Boolean.TRUE.equals(acquired)) {
                    log.debug("Redis lock acquired for seat {} on event {}", seatId, eventId);
                    return true;
                }
                log.warn("Redis lock already held for seat {} on event {}", seatId, eventId);
                return false;
            }
        } catch (Exception ex) {
            markRedisDown(ex);
        }
        return memory.lockSeat(eventId, seatId, userId, bookingId);
    }

    @Override
    public void unlockSeat(Long eventId, Long seatId) {
        String key = InMemorySeatLockService.key(eventId, seatId);
        try {
            if (redisReady()) {
                redis.delete(key);
            }
        } catch (Exception ex) {
            markRedisDown(ex);
        }
        memory.unlockSeat(eventId, seatId);
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
        String key = InMemorySeatLockService.key(eventId, seatId);
        try {
            if (redisReady()) {
                Boolean has = redis.hasKey(key);
                if (Boolean.TRUE.equals(has)) return true;
            }
        } catch (Exception ex) {
            markRedisDown(ex);
        }
        return memory.isSeatLocked(eventId, seatId);
    }

    @Override
    public String getLockHolder(Long eventId, Long seatId) {
        String key = InMemorySeatLockService.key(eventId, seatId);
        try {
            if (redisReady()) {
                String holder = redis.opsForValue().get(key);
                if (holder != null) return holder;
            }
        } catch (Exception ex) {
            markRedisDown(ex);
        }
        return memory.getLockHolder(eventId, seatId);
    }

    @Override
    public long getLockTTL(Long eventId, Long seatId) {
        String key = InMemorySeatLockService.key(eventId, seatId);
        try {
            if (redisReady()) {
                Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) return ttl;
            }
        } catch (Exception ex) {
            markRedisDown(ex);
        }
        return memory.getLockTTL(eventId, seatId);
    }
}
