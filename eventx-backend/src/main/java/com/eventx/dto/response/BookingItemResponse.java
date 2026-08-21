package com.eventx.dto.response;

import com.eventx.entity.enums.SeatCategory;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemResponse {
    private Long id;
    private String seatNumber;
    private String row;
    private String section;
    private SeatCategory seatCategory;
    private String ticketCategoryName;
    private BigDecimal price;
}
