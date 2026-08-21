package com.eventx.controller;

import com.eventx.dto.request.VerifyTicketRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.TicketVerificationResponse;
import com.eventx.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets - Admin")
public class AdminTicketController {

    private final TicketService ticketService;

    @PostMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify and scan a ticket")
    public ResponseEntity<ApiResponse<TicketVerificationResponse>> verifyTicket(
            @Valid @RequestBody VerifyTicketRequest request) {
        TicketVerificationResponse response = ticketService.verifyTicket(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
