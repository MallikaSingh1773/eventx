package com.eventx.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {
    @NotBlank(message = "Razorpay Order ID cannot be blank")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID cannot be blank")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature cannot be blank")
    private String razorpaySignature;
}
