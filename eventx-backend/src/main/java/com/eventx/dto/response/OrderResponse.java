package com.eventx.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private Long bookingId;
    private String bookingStatus;
    private Integer amountInPaise;
}
