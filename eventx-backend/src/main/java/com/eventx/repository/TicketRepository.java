package com.eventx.repository;

import com.eventx.entity.Ticket;
import com.eventx.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBookingId(Long bookingId);
    Optional<Ticket> findByTicketCode(String ticketCode);
    long countByStatus(TicketStatus status);
}
