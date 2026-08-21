package com.eventx.controller;

import com.eventx.dto.request.CreateVenueRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.VenueResponse;
import com.eventx.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer/venues")
@Tag(name = "Venues - Organizer")
@RequiredArgsConstructor
public class OrganizerVenueController {

    private final VenueService venueService;

    @PostMapping
    @Operation(summary = "Add a venue with a default seat map")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        return new ResponseEntity<>(
                ApiResponse.success("Venue created", venueService.createVenueWithDefaultSeats(request)),
                HttpStatus.CREATED);
    }
}
