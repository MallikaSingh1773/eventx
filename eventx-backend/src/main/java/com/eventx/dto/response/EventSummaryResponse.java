package com.eventx.dto.response;

import com.eventx.entity.enums.EventCategory;
import com.eventx.entity.enums.EventStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class EventSummaryResponse {
    private Long id;
    private String title;
    private EventCategory category;
    private String imageUrl;
    private String venueName;
    private String city;
    private LocalDate eventDate;
    private LocalTime startTime;
    private EventStatus status;
    private String organizerName;
    private BigDecimal startingPrice;
    private int totalSeats;
    private int availableSeats;
}
