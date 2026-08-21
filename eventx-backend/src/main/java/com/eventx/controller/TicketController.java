package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.TicketResponse;
import com.eventx.security.CustomUserDetailsService.CustomUserDetails;
import com.eventx.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        TicketResponse ticket = ticketService.getTicket(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get all tickets for a booking")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsForBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<TicketResponse> tickets = ticketService.getTicketsForBooking(bookingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }
}
