package com.eventx.controller;

import com.eventx.dto.request.CreateEventRequest;
import com.eventx.dto.request.UpdateEventRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.EventResponse;
import com.eventx.dto.response.EventSummaryResponse;
import com.eventx.service.EventService;
import com.eventx.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Events - Admin")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create an event")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Event created successfully", eventService.createEvent(request, SecurityUtils.getCurrentUserId())), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all events")
    public ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> getAllEvents(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getAllEvents(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by id")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEvent(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", eventService.updateEvent(id, request, SecurityUtils.getCurrentUserId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }
}
