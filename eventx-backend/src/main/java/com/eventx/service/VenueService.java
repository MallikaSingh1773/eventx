package com.eventx.service;

import com.eventx.dto.request.CreateSeatsRequest;
import com.eventx.dto.request.CreateVenueRequest;
import com.eventx.dto.response.SeatResponse;
import com.eventx.dto.response.VenueResponse;

import java.util.List;

public interface VenueService {
    VenueResponse createVenue(CreateVenueRequest request);
    VenueResponse updateVenue(Long id, CreateVenueRequest request);
    VenueResponse getVenue(Long id);
    List<VenueResponse> getAllVenues();
    List<SeatResponse> createSeats(CreateSeatsRequest request);
    List<SeatResponse> getSeats(Long venueId);
    VenueResponse createVenueWithDefaultSeats(CreateVenueRequest request);
}
