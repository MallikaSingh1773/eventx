package com.eventx.controller;

import com.eventx.dto.request.RefundRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.RefundResponse;
import com.eventx.security.CustomUserDetailsService.CustomUserDetails;
import com.eventx.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Refunds")
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{id}/refund")
    @Operation(summary = "Process refund for a booking")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable Long id,
            @RequestBody RefundRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        RefundResponse response = refundService.processRefund(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
