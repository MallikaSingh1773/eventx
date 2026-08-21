package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.EventResponse;
import com.eventx.dto.response.EventSummaryResponse;
import com.eventx.dto.response.SeatResponse;
import com.eventx.entity.enums.EventCategory;
import com.eventx.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events - Public")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Get published events")
    public ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> getPublishedEvents(
            @PageableDefault(size = 20, sort = "eventDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getPublishedEvents(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by id")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEvent(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search events")
    public ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> searchEvents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "eventDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(eventService.searchEvents(query, category, city, dateFrom, dateTo, pageable)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events")
    public ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> getUpcomingEvents(
            @PageableDefault(size = 10, sort = "eventDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getUpcomingEvents(pageable)));
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "Get available seats for event")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeats(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getAvailableSeats(id)));
    }
}
