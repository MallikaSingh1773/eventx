package com.eventx.service;

import java.util.List;

public interface SeatLockService {
    boolean lockSeat(Long eventId, Long seatId, Long userId, Long bookingId);
    void unlockSeat(Long eventId, Long seatId);
    void unlockSeatsForBooking(Long eventId, List<Long> seatIds);
    boolean isSeatLocked(Long eventId, Long seatId);
    String getLockHolder(Long eventId, Long seatId);
    long getLockTTL(Long eventId, Long seatId);
}
