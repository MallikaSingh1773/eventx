package com.eventx.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionRequest {
    @NotNull(message = "Seat ID cannot be null")
    private Long seatId;

    @NotNull(message = "Ticket Category ID cannot be null")
    private Long ticketCategoryId;
}
