package com.eventx.dto.request;

import com.eventx.entity.enums.SeatCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketCategoryRequest {
    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    @Positive
    private int totalSeats;

    @NotNull
    private SeatCategory seatCategory;
}
