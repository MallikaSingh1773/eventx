package com.eventx.dto.request;

import com.eventx.entity.enums.EventCategory;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateEventRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private EventCategory category;

    private String imageUrl;

    @NotNull
    private Long venueId;

    @NotNull
    @FutureOrPresent
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDateTime bookingOpenTime;
    private LocalDateTime bookingCloseTime;

    @NotEmpty
    private List<TicketCategoryRequest> ticketCategories;

    /** When true, the event is visible on the public listing immediately. */
    private Boolean publish = Boolean.TRUE;
}
