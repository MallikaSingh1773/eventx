package com.eventx.dto.response;

import com.eventx.entity.enums.SeatCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TicketCategoryResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private int totalSeats;
    private int availableSeats;
    private SeatCategory seatCategory;
}
