package com.eventx.dto.response;

import com.eventx.entity.enums.EventCategory;
import com.eventx.entity.enums.EventStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private EventCategory category;
    private String imageUrl;
    private VenueResponse venue;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime bookingOpenTime;
    private LocalDateTime bookingCloseTime;
    private EventStatus status;
    private Long organizerId;
    private String organizerName;
    private List<TicketCategoryResponse> ticketCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
