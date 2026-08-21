package com.eventx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewStatsResponse {
    private long totalUsers;
    private long totalEvents;
    private long publishedEvents;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunded;
    private long totalTicketsSold;
    private long upcomingEvents;
}
