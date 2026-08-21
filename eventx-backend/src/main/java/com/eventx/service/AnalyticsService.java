package com.eventx.service;

import com.eventx.dto.response.EventStatsResponse;
import com.eventx.dto.response.OverviewStatsResponse;
import com.eventx.dto.response.RevenueDataPoint;

import java.util.List;

public interface AnalyticsService {
    OverviewStatsResponse getOverview();
    List<RevenueDataPoint> getRevenueData(String period);
    List<EventStatsResponse> getEventStats();
}
