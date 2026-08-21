package com.eventx.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsResponse {
    private Long bookingId;
    private LocalDateTime lockedUntil;
    private long ttlSeconds;
    private BigDecimal totalAmount;
    private List<BookingItemResponse> items;
}
