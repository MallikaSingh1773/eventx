package com.eventx.controller;

import com.eventx.dto.request.CreateOrderRequest;
import com.eventx.dto.request.PaymentVerificationRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.OrderResponse;
import com.eventx.dto.response.PaymentResponse;
import com.eventx.service.PaymentService;
import com.eventx.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = paymentService.createOrder(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(@Valid @RequestBody PaymentVerificationRequest request) {
        PaymentResponse response = paymentService.verifyPayment(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable Long bookingId) {
        PaymentResponse response = paymentService.getStatus(bookingId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** Local-only convenience endpoint. It is used when no Razorpay keys are configured. */
    @PostMapping("/demo-pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> completeDemoPayment(@Valid @RequestBody CreateOrderRequest request) {
        PaymentResponse response = paymentService.completeDemoPayment(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Demo payment completed", response));
    }
}
