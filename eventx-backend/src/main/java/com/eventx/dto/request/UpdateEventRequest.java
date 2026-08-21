package com.eventx.dto.request;

import com.eventx.entity.enums.EventCategory;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class UpdateEventRequest {
    private String title;
    private String description;
    private EventCategory category;
    private String imageUrl;
    private Long venueId;

    @FutureOrPresent
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDateTime bookingOpenTime;
    private LocalDateTime bookingCloseTime;

    private List<TicketCategoryRequest> ticketCategories;
}
