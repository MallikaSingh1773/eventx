package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.EventStatsResponse;
import com.eventx.dto.response.OverviewStatsResponse;
import com.eventx.dto.response.RevenueDataPoint;
import com.eventx.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics - Admin")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get overview statistics")
    public ResponseEntity<ApiResponse<OverviewStatsResponse>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getOverview()));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get revenue data points")
    public ResponseEntity<ApiResponse<List<RevenueDataPoint>>> getRevenueData(
            @RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRevenueData(period)));
    }

    @GetMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get detailed event statistics")
    public ResponseEntity<ApiResponse<List<EventStatsResponse>>> getEventStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getEventStats()));
    }
}
