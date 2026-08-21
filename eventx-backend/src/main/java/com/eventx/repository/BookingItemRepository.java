package com.eventx.repository;

import com.eventx.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    List<BookingItem> findByBookingId(Long bookingId);
    
    @Query("SELECT bi FROM BookingItem bi WHERE bi.seat.id IN :seatIds AND bi.booking.event.id = :eventId AND bi.booking.status IN ('CONFIRMED', 'PENDING', 'PAYMENT_PENDING')")
    List<BookingItem> findActiveBySeatIdsAndEventId(@Param("seatIds") List<Long> seatIds, @Param("eventId") Long eventId);

    @Query("SELECT bi.seat.id FROM BookingItem bi WHERE bi.booking.event.id = :eventId AND bi.booking.status = 'CONFIRMED'")
    List<Long> findConfirmedSeatIdsByEventId(@Param("eventId") Long eventId);
}
