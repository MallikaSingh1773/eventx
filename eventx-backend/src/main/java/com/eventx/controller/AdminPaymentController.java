package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.entity.Payment;
import com.eventx.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Payments - Admin")
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payments")
    public ResponseEntity<ApiResponse<Page<Payment>>> getAllPayments(Pageable pageable) {
        // Exposing entity directly here per request logic, although mapping to DTO is preferred
        return ResponseEntity.ok(ApiResponse.success(paymentRepository.findAll(pageable)));
    }
}
