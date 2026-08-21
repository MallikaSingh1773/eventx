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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/events")
@Tag(name = "Events - Organizer")
@RequiredArgsConstructor
public class OrganizerEventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "List events posted by the current organizer")
    public ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> myEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                eventService.getEventsByOrganizer(SecurityUtils.getCurrentUserId(), pageable)));
    }

    @PostMapping
    @Operation(summary = "Post a new event")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return new ResponseEntity<>(
                ApiResponse.success("Event posted successfully",
                        eventService.createEvent(request, SecurityUtils.getCurrentUserId())),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one of your events")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully",
                eventService.updateEvent(id, request, SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a draft event")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event published",
                eventService.publishEvent(id, SecurityUtils.getCurrentUserId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel one of your events")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", null));
    }
}
