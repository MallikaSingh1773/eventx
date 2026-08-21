package com.eventx.service.impl;

import com.eventx.dto.request.LockSeatsRequest;
import com.eventx.dto.request.SeatSelectionRequest;
import com.eventx.dto.response.BookingItemResponse;
import com.eventx.dto.response.BookingResponse;
import com.eventx.dto.response.LockSeatsResponse;
import com.eventx.entity.*;
import com.eventx.entity.enums.BookingStatus;
import com.eventx.entity.enums.EventStatus;
import com.eventx.exception.BadRequestException;
import com.eventx.exception.BookingException;
import com.eventx.exception.ResourceNotFoundException;
import com.eventx.exception.SeatLockException;
import com.eventx.repository.*;
import com.eventx.service.BookingService;
import com.eventx.service.SeatLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final SeatLockService seatLockService;
    private final UserRepository userRepository;
    private final int lockTtlSeconds;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingItemRepository bookingItemRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository,
            TicketCategoryRepository ticketCategoryRepository,
            SeatLockService seatLockService,
            UserRepository userRepository,
            @Value("${app.seat-lock.ttl-seconds:600}") int lockTtlSeconds) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.seatLockService = seatLockService;
        this.userRepository = userRepository;
        this.lockTtlSeconds = lockTtlSeconds;
    }

    @Override
    public LockSeatsResponse lockSeats(LockSeatsRequest request, Long userId) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BookingException("Event is not published");
        }

        LocalDateTime now = LocalDateTime.now();
        if (event.getBookingOpenTime() != null && event.getBookingCloseTime() != null) {
            if (now.isBefore(event.getBookingOpenTime()) || now.isAfter(event.getBookingCloseTime())) {
                throw new BookingException("Booking is currently closed for this event");
            }
        } else if (event.getEventDate().isBefore(LocalDate.now())) {
            throw new BookingException("Cannot book past events");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Long> requestedSeatIds = request.getSeats().stream()
                .map(SeatSelectionRequest::getSeatId)
                .toList();

        List<BookingItem> existingBookings = bookingItemRepository.findActiveBySeatIdsAndEventId(requestedSeatIds, event.getId());
        if (!existingBookings.isEmpty()) {
            throw new SeatLockException("One or more selected seats are already booked");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> itemsToSave = new ArrayList<>();
        
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(BigDecimal.ZERO);
        
        booking = bookingRepository.save(booking);

        List<Long> lockedSeatIds = new ArrayList<>();

        try {
            for (SeatSelectionRequest seatReq : request.getSeats()) {
                Seat seat = seatRepository.findById(seatReq.getSeatId())
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatReq.getSeatId()));
                
                if (!seat.getVenue().getId().equals(event.getVenue().getId())) {
                    throw new BadRequestException("Seat does not belong to the event venue");
                }

                TicketCategory ticketCategory = ticketCategoryRepository.findById(seatReq.getTicketCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ticket category not found: " + seatReq.getTicketCategoryId()));
                
                if (!ticketCategory.getEvent().getId().equals(event.getId())) {
                    throw new BadRequestException("Ticket category does not belong to this event");
                }

                boolean locked = seatLockService.lockSeat(event.getId(), seat.getId(), userId, booking.getId());
                if (!locked) {
                    throw new SeatLockException("Seat " + seat.getSeatNumber() + " is already locked by another user");
                }
                lockedSeatIds.add(seat.getId());

                totalAmount = totalAmount.add(ticketCategory.getPrice());

                BookingItem item = new BookingItem();
                item.setBooking(booking);
                item.setSeat(seat);
                item.setTicketCategory(ticketCategory);
                item.setPrice(ticketCategory.getPrice());
                itemsToSave.add(item);
            }

            bookingItemRepository.saveAll(itemsToSave);
            
            booking.setTotalAmount(totalAmount);
            booking.setLockedUntil(now.plusSeconds(lockTtlSeconds));
            booking = bookingRepository.save(booking);

            List<BookingItemResponse> itemResponses = itemsToSave.stream()
                    .map(this::mapToBookingItemResponse)
                    .collect(Collectors.toList());

            return LockSeatsResponse.builder()
                    .bookingId(booking.getId())
                    .lockedUntil(booking.getLockedUntil())
                    .ttlSeconds(lockTtlSeconds)
                    .totalAmount(totalAmount)
                    .items(itemResponses)
                    .build();

        } catch (Exception e) {
            seatLockService.unlockSeatsForBooking(event.getId(), lockedSeatIds);
            bookingRepository.delete(booking);
            throw e;
        }
    }

    @Override
    public BookingResponse getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        if (userId != null && !booking.getUser().getId().equals(userId)) {
            throw new BookingException("Access denied to this booking");
        }
        
        return mapToBookingResponse(booking);
    }

    @Override
    public Page<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable)
                .map(this::mapToBookingResponse);
    }

    @Override
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(this::mapToBookingResponse);
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return mapToBookingResponse(booking);
        }
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new BookingException("Booking is not in PAYMENT_PENDING state");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        
        List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
        List<Long> seatIdsToUnlock = new ArrayList<>();
        
        for (BookingItem item : items) {
            TicketCategory category = item.getTicketCategory();
            if (category.getAvailableSeats() > 0) {
                category.setAvailableSeats(category.getAvailableSeats() - 1);
                ticketCategoryRepository.save(category);
            }
            seatIdsToUnlock.add(item.getSeat().getId());
        }

        seatLockService.unlockSeatsForBooking(booking.getEvent().getId(), seatIdsToUnlock);
        
        booking = bookingRepository.save(booking);
        return mapToBookingResponse(booking);
    }

    @Override
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingException("Access denied to this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BookingException("Booking is already cancelled or expired");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            LocalDateTime eventDateTime = booking.getEvent().getEventDate().atStartOfDay();
            if (LocalDateTime.now().plusHours(24).isAfter(eventDateTime)) {
                throw new BookingException("Cannot cancel booking within 24 hours of event");
            }
            booking.setStatus(BookingStatus.REFUND_PENDING);
            
            List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
            for (BookingItem item : items) {
                TicketCategory category = item.getTicketCategory();
                category.setAvailableSeats(category.getAvailableSeats() + 1);
                ticketCategoryRepository.save(category);
            }
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
            List<Long> seatIds = items.stream().map(i -> i.getSeat().getId()).toList();
            seatLockService.unlockSeatsForBooking(booking.getEvent().getId(), seatIds);
        }

        booking = bookingRepository.save(booking);
        return mapToBookingResponse(booking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getId());
        List<BookingItemResponse> itemResponses = items.stream()
                .map(this::mapToBookingItemResponse)
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .eventDate(booking.getEvent().getEventDate())
                .venueName(booking.getEvent().getVenue().getName())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .razorpayOrderId(booking.getRazorpayOrderId())
                .items(itemResponses)
                .lockedUntil(booking.getLockedUntil())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private BookingItemResponse mapToBookingItemResponse(BookingItem item) {
        return BookingItemResponse.builder()
                .id(item.getId())
                .seatNumber(item.getSeat().getSeatNumber())
                .row(item.getSeat().getRow())
                .section(item.getSeat().getSection())
                .seatCategory(item.getSeat().getCategory())
                .ticketCategoryName(item.getTicketCategory().getName())
                .price(item.getPrice())
                .build();
    }
}

