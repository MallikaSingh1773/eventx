package com.eventx.service;

import com.eventx.dto.request.VerifyTicketRequest;
import com.eventx.dto.response.TicketResponse;
import com.eventx.dto.response.TicketVerificationResponse;

import java.util.List;

public interface TicketService {
    List<TicketResponse> generateTickets(Long bookingId);
    TicketResponse getTicket(Long ticketId, Long userId);
    List<TicketResponse> getTicketsForBooking(Long bookingId, Long userId);
    TicketVerificationResponse verifyTicket(VerifyTicketRequest request);
}
