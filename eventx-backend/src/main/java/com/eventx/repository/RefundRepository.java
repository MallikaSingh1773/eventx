package com.eventx.repository;

import com.eventx.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByBookingId(Long bookingId);
    
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.status = 'PROCESSED'")
    BigDecimal getTotalRefunded();
}
