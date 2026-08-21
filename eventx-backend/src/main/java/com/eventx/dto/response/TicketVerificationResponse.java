package com.eventx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketVerificationResponse {
    private String status; // VALID, ALREADY_USED, CANCELLED, EXPIRED, NOT_FOUND
    private String message;
    private TicketResponse ticketDetails;
}
