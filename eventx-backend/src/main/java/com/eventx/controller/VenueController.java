package com.eventx.controller;

import com.eventx.dto.response.ApiResponse;
import com.eventx.dto.response.VenueResponse;
import com.eventx.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues - Public")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    @Operation(summary = "List venues that organizers can book")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getVenues() {
        return ResponseEntity.ok(ApiResponse.success(venueService.getAllVenues()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a venue")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getVenue(id)));
    }
}
