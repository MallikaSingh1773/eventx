package com.eventx.service;

import com.eventx.entity.Booking;
import com.eventx.entity.BookingItem;
import com.eventx.entity.TicketCategory;
import com.eventx.entity.enums.BookingStatus;
import com.eventx.repository.BookingItemRepository;
import com.eventx.repository.BookingRepository;
import com.eventx.repository.TicketCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingCleanupService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final SeatLockService seatLockService;

    public BookingCleanupService(
            BookingRepository bookingRepository,
            BookingItemRepository bookingItemRepository,
            TicketCategoryRepository ticketCategoryRepository,
            SeatLockService seatLockService) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.seatLockService = seatLockService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndLockedUntilBefore(com.eventx.entity.enums.BookingStatus.PENDING, now);
        
        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired bookings to clean up", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                booking.setStatus(BookingStatus.EXPIRED);
                
                List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getId());
                List<Long> seatIds = items.stream().map(i -> i.getSeat().getId()).toList();
                
                seatLockService.unlockSeatsForBooking(booking.getEvent().getId(), seatIds);

                bookingRepository.save(booking);
                log.info("Cleaned up expired booking ID: {}", booking.getId());
            } catch (Exception e) {
                log.error("Failed to clean up booking ID: {}", booking.getId(), e);
            }
        }
    }
}

