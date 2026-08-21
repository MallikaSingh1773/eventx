package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.RefundResponse;
import com.eventx.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@Tag(name = "Refunds - Admin")
public class AdminRefundController {

    private final RefundService refundService;

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all refunds")
    public ResponseEntity<ApiResponse<Page<RefundResponse>>> getAllRefunds(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(refundService.getAllRefunds(pageable)));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get refunds for a specific booking")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundsForBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(refundService.getRefundsForBooking(bookingId)));
    }
}
