package com.eventx.service.impl;

import com.eventx.dto.request.VerifyTicketRequest;
import com.eventx.dto.response.TicketResponse;
import com.eventx.dto.response.TicketVerificationResponse;
import com.eventx.entity.Booking;
import com.eventx.entity.BookingItem;
import com.eventx.entity.Ticket;
import com.eventx.entity.enums.BookingStatus;
import com.eventx.entity.enums.TicketStatus;
import com.eventx.exception.ResourceNotFoundException;
import com.eventx.exception.BadRequestException;
import com.eventx.repository.BookingRepository;
import com.eventx.repository.BookingItemRepository;
import com.eventx.repository.TicketRepository;
import com.eventx.service.TicketService;
import com.eventx.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final QRCodeGenerator qrCodeGenerator;

    @Override
    public List<TicketResponse> generateTickets(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Tickets can only be generated for confirmed bookings");
        }

        List<Ticket> existing = ticketRepository.findByBookingId(bookingId);
        if (!existing.isEmpty()) {
            return existing.stream().map(this::mapToTicketResponse).collect(Collectors.toList());
        }

        List<BookingItem> items = bookingItemRepository.findByBookingId(bookingId);
        List<TicketResponse> responses = new ArrayList<>();

        for (BookingItem item : items) {
            String ticketCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
            String qrCodeData = qrCodeGenerator.generateQRCodeBase64(ticketCode, 300, 300);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setBookingItem(item);
            ticket.setTicketCode(ticketCode);
            ticket.setQrCodeData(qrCodeData);
            ticket.setStatus(TicketStatus.VALID);
            
            Ticket savedTicket = ticketRepository.save(ticket);
            responses.add(mapToTicketResponse(savedTicket));
        }

        return responses;
    }

    @Override
    public TicketResponse getTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        // Validate user owns the booking or is admin - Assuming a simple check here
        if (!ticket.getBooking().getUser().getId().equals(userId)) {
            // Need a role check ideally, but we'll stick to user ownership for this basic check
            throw new BadRequestException("User does not have permission to view this ticket");
        }

        return mapToTicketResponse(ticket);
    }

    @Override
    public List<TicketResponse> getTicketsForBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("User does not have permission to view tickets for this booking");
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        return tickets.stream().map(this::mapToTicketResponse).collect(Collectors.toList());
    }

    @Override
    public TicketVerificationResponse verifyTicket(VerifyTicketRequest request) {
        Ticket ticket = ticketRepository.findByTicketCode(request.getTicketCode()).orElse(null);

        if (ticket == null) {
            return TicketVerificationResponse.builder()
                    .status("NOT_FOUND")
                    .message("Ticket with given code not found")
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            return TicketVerificationResponse.builder()
                    .status("CANCELLED")
                    .message("Ticket has been cancelled")
                    .ticketDetails(mapToTicketResponse(ticket))
                    .build();
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            return TicketVerificationResponse.builder()
                    .status("ALREADY_USED")
                    .message("Ticket has already been used")
                    .ticketDetails(mapToTicketResponse(ticket))
                    .build();
        }

        if (ticket.getBooking().getEvent().getEventDate().isBefore(LocalDate.now())) {
            return TicketVerificationResponse.builder()
                    .status("EXPIRED")
                    .message("Ticket event date has passed")
                    .ticketDetails(mapToTicketResponse(ticket))
                    .build();
        }

        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);

        return TicketVerificationResponse.builder()
                .status("VALID")
                .message("Ticket is valid and marked as used")
                .ticketDetails(mapToTicketResponse(ticket))
                .build();
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        BookingItem item = ticket.getBookingItem();
        Booking booking = ticket.getBooking();
        
        return TicketResponse.builder()
                .id(ticket.getId())
                .bookingId(booking.getId())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .eventDate(booking.getEvent().getEventDate())
                .venueName(booking.getEvent().getVenue().getName())
                .seatNumber(item.getSeat().getSeatNumber())
                .row(item.getSeat().getRow())
                .section(item.getSeat().getSection())
                .seatCategory(item.getSeat().getCategory())
                .ticketCategoryName(item.getTicketCategory().getName())
                .price(item.getPrice())
                .ticketCode(ticket.getTicketCode())
                .qrCodeData(ticket.getQrCodeData())
                .status(ticket.getStatus())
                .attendeeName(booking.getUser().getName()) // or attendee info if separate
                .attendeeEmail(booking.getUser().getEmail())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}

