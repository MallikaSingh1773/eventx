package com.eventx.controller;

import com.eventx.dto.request.CreateSeatsRequest;
import com.eventx.dto.request.CreateVenueRequest;
import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.SeatResponse;
import com.eventx.dto.response.VenueResponse;
import com.eventx.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/venues")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Venues - Admin")
@RequiredArgsConstructor
@Slf4j
public class AdminVenueController {

    private final VenueService venueService;

    @PostMapping
    @Operation(summary = "Create a venue")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Venue created successfully", venueService.createVenue(request)), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all venues")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues() {
        return ResponseEntity.ok(ApiResponse.success(venueService.getAllVenues()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue by id")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getVenue(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a venue")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(@PathVariable Long id, @Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Venue updated successfully", venueService.updateVenue(id, request)));
    }

    @PostMapping("/seats")
    @Operation(summary = "Bulk create seats for a venue")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createSeats(@Valid @RequestBody CreateSeatsRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Seats created successfully", venueService.createSeats(request)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "Get all seats for a venue")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeats(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getSeats(id)));
    }
}
