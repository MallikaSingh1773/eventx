package com.eventx.dto.response;

import com.eventx.entity.enums.BookingStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private String venueName;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String razorpayOrderId;
    private List<BookingItemResponse> items;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
