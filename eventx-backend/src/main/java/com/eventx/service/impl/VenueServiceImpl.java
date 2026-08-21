package com.eventx.service.impl;

import com.eventx.dto.request.CreateSeatsRequest;
import com.eventx.dto.request.CreateVenueRequest;
import com.eventx.dto.response.SeatResponse;
import com.eventx.dto.response.VenueResponse;
import com.eventx.entity.Seat;
import com.eventx.entity.Venue;
import com.eventx.entity.enums.SeatCategory;
import com.eventx.exception.ResourceNotFoundException;
import com.eventx.repository.SeatRepository;
import com.eventx.repository.VenueRepository;
import com.eventx.service.VenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    @Override
    public VenueResponse createVenue(CreateVenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setState(request.getState());
        venue.setCapacity(request.getCapacity());
        venue.setDescription(request.getDescription());

        Venue savedVenue = venueRepository.save(venue);
        return mapToResponse(savedVenue);
    }

    @Override
    public VenueResponse createVenueWithDefaultSeats(CreateVenueRequest request) {
        VenueResponse created = createVenue(request);
        Venue venue = venueRepository.findById(created.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
        int vip = Math.max(5, request.getCapacity() / 10);
        int premium = Math.max(8, request.getCapacity() / 5);
        int regular = Math.max(10, request.getCapacity() / 2);
        addSeatBlock(venue, SeatCategory.VIP, "VIP", "A", "V", vip);
        addSeatBlock(venue, SeatCategory.PREMIUM, "PREMIUM", "B", "P", premium);
        addSeatBlock(venue, SeatCategory.REGULAR, "REGULAR", "C", "R", regular);
        return created;
    }

    private void addSeatBlock(Venue venue, SeatCategory category, String section, String row, String prefix, int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat();
            seat.setVenue(venue);
            seat.setCategory(category);
            seat.setSection(section);
            seat.setRow(row);
            seat.setSeatNumber(prefix + i);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);
    }

    @Override
    public VenueResponse updateVenue(Long id, CreateVenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setState(request.getState());
        venue.setCapacity(request.getCapacity());
        venue.setDescription(request.getDescription());

        Venue updatedVenue = venueRepository.save(venue);
        return mapToResponse(updatedVenue);
    }

    @Override
    @Transactional(readOnly = true)
    public VenueResponse getVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        return mapToResponse(venue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> createSeats(CreateSeatsRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));

        List<Seat> seatsToSave = new ArrayList<>();
        
        for (int i = request.getStartNumber(); i <= request.getEndNumber(); i++) {
            Seat seat = new Seat();
            seat.setVenue(venue);
            seat.setCategory(request.getCategory());
            seat.setSection(request.getSection());
            seat.setRow(request.getRowPrefix());
            seat.setSeatNumber(request.getRowPrefix() + i);
            seatsToSave.add(seat);
        }

        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);

        return savedSeats.stream().map(seat -> SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .row(seat.getRow())
                .section(seat.getSection())
                .category(seat.getCategory())
                .available(true)
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long venueId) {
        List<Seat> seats = seatRepository.findByVenueId(venueId);
        return seats.stream().map(seat -> SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .row(seat.getRow())
                .section(seat.getSection())
                .category(seat.getCategory())
                .available(true) // Context-free getSeats implies static availability or standard setup
                .build()).collect(Collectors.toList());
    }

    private VenueResponse mapToResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .state(venue.getState())
                .capacity(venue.getCapacity())
                .description(venue.getDescription())
                .build();
    }
}
