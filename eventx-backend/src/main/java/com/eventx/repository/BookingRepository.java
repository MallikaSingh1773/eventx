package com.eventx.repository;

import com.eventx.entity.Booking;
import com.eventx.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    Page<Booking> findByEventId(Long eventId, Pageable pageable);
    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);
    long countByStatus(BookingStatus status);
    
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.event.id = :eventId AND b.status NOT IN ('CANCELLED', 'EXPIRED', 'REFUNDED')")
    long countActiveBookingsByUserAndEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    List<Booking> findByStatusAndLockedUntilBefore(BookingStatus status, LocalDateTime time);
    
    @Query("SELECT DATE(b.createdAt) as date, COUNT(b) as count, COALESCE(SUM(b.totalAmount), 0) as revenue FROM Booking b WHERE b.status = 'CONFIRMED' AND b.createdAt >= :startDate GROUP BY DATE(b.createdAt) ORDER BY DATE(b.createdAt)")
    List<Object[]> getBookingStats(@Param("startDate") LocalDateTime startDate);
}
