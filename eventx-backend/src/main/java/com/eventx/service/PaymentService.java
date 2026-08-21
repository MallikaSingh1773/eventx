package com.eventx.service;

import com.eventx.dto.request.CreateOrderRequest;
import com.eventx.dto.request.PaymentVerificationRequest;
import com.eventx.dto.response.OrderResponse;
import com.eventx.dto.response.PaymentResponse;

public interface PaymentService {
    OrderResponse createOrder(CreateOrderRequest request, Long userId);
    PaymentResponse verifyPayment(PaymentVerificationRequest request, Long userId);
    PaymentResponse getStatus(Long bookingId, Long userId);
    PaymentResponse completeDemoPayment(CreateOrderRequest request, Long userId);
    void handleWebhook(String payload, String signature);
}
