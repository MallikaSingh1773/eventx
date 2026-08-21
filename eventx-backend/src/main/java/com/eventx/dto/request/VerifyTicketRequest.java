package com.eventx.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTicketRequest {
    @NotBlank(message = "Ticket code is required")
    private String ticketCode;
}
