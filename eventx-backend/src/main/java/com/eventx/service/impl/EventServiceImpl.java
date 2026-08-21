package com.eventx.service.impl;

import com.eventx.dto.request.CreateEventRequest;
import com.eventx.dto.request.UpdateEventRequest;
import com.eventx.dto.response.EventResponse;
import com.eventx.dto.response.EventSummaryResponse;
import com.eventx.dto.response.SeatResponse;
import com.eventx.dto.response.TicketCategoryResponse;
import com.eventx.dto.response.VenueResponse;
import com.eventx.entity.Event;
import com.eventx.entity.Seat;
import com.eventx.entity.TicketCategory;
import com.eventx.entity.User;
import com.eventx.entity.Venue;
import com.eventx.entity.enums.EventCategory;
import com.eventx.entity.enums.EventStatus;
import com.eventx.entity.enums.Role;
import com.eventx.exception.ResourceNotFoundException;
import com.eventx.exception.UnauthorizedException;
import com.eventx.repository.BookingItemRepository;
import com.eventx.repository.EventRepository;
import com.eventx.repository.SeatRepository;
import com.eventx.repository.UserRepository;
import com.eventx.repository.VenueRepository;
import com.eventx.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.eventx.service.SeatLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final BookingItemRepository bookingItemRepository;
    private final SeatLockService seatLockService;
    private final UserRepository userRepository;

    @Override
    public EventResponse createEvent(CreateEventRequest request, Long organizerId) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setImageUrl(request.getImageUrl());
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setBookingOpenTime(request.getBookingOpenTime() != null ? request.getBookingOpenTime() : LocalDateTime.now());
        event.setBookingCloseTime(request.getBookingCloseTime() != null
                ? request.getBookingCloseTime()
                : request.getEventDate().atTime(request.getStartTime() != null ? request.getStartTime() : java.time.LocalTime.NOON));
        boolean publish = request.getPublish() == null || request.getPublish();
        event.setStatus(publish ? EventStatus.PUBLISHED : EventStatus.DRAFT);

        List<TicketCategory> ticketCategories = request.getTicketCategories().stream().map(tcRequest -> {
            TicketCategory tc = new TicketCategory();
            tc.setEvent(event);
            tc.setName(tcRequest.getName());
            tc.setPrice(tcRequest.getPrice());
            tc.setTotalSeats(tcRequest.getTotalSeats());
            tc.setAvailableSeats(tcRequest.getTotalSeats());
            tc.setSeatCategory(tcRequest.getSeatCategory());
            return tc;
        }).collect(Collectors.toList());

        event.setTicketCategories(ticketCategories);
        Event savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    @Override
    public EventResponse updateEvent(Long id, UpdateEventRequest request, Long actorId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        assertCanManage(event, actorId);

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategory() != null) event.setCategory(request.getCategory());
        if (request.getImageUrl() != null) event.setImageUrl(request.getImageUrl());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getBookingOpenTime() != null) event.setBookingOpenTime(request.getBookingOpenTime());
        if (request.getBookingCloseTime() != null) event.setBookingCloseTime(request.getBookingCloseTime());

        if (request.getVenueId() != null) {
            Venue venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));
            event.setVenue(venue);
        }

        // Updating ticket categories usually requires more complex merge logic, omitting for brevity or recreating
        if (request.getTicketCategories() != null) {
            event.getTicketCategories().clear();
            List<TicketCategory> updatedCategories = request.getTicketCategories().stream().map(tcRequest -> {
                TicketCategory tc = new TicketCategory();
                tc.setEvent(event);
                tc.setName(tcRequest.getName());
                tc.setPrice(tcRequest.getPrice());
                tc.setTotalSeats(tcRequest.getTotalSeats());
                tc.setAvailableSeats(tcRequest.getTotalSeats()); // Simple overwrite, real app needs careful update
                tc.setSeatCategory(tcRequest.getSeatCategory());
                return tc;
            }).collect(Collectors.toList());
            event.getTicketCategories().addAll(updatedCategories);
        }

        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Override
    public void deleteEvent(Long id, Long actorId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        assertCanManage(event, actorId);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Override
    public EventResponse publishEvent(Long id, Long actorId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        assertCanManage(event, actorId);
        event.setStatus(EventStatus.PUBLISHED);
        if (event.getBookingOpenTime() == null) {
            event.setBookingOpenTime(LocalDateTime.now());
        }
        return mapToResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(this::mapToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getEventsByOrganizer(Long organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable).map(this::mapToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getPublishedEvents(Pageable pageable) {
        // Assuming findByStatus exists in repository or using custom spec, here using standard method pattern
        // You might need to add this method in EventRepository: Page<Event> findByStatus(EventStatus status, Pageable pageable);
        // Using a Specification for safety if not defined in repository
        Specification<Event> spec = (root, query, cb) -> cb.equal(root.get("status"), EventStatus.PUBLISHED);
        return eventRepository.findAll(spec, pageable).map(this::mapToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> searchEvents(String query, EventCategory category, String city, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Specification<Event> spec = Specification.where(null);
        
        spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), EventStatus.PUBLISHED));

        if (query != null && !query.isEmpty()) {
            spec = spec.and((root, cq, cb) -> cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"));
        }
        if (category != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("category"), category));
        }
        if (city != null && !city.isEmpty()) {
            spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("venue").get("city")), city.toLowerCase()));
        }
        if (dateFrom != null) {
            spec = spec.and((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), dateTo));
        }

        return eventRepository.findAll(spec, pageable).map(this::mapToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> getUpcomingEvents(Pageable pageable) {
        return eventRepository.findUpcoming(LocalDate.now(), pageable).map(this::mapToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        
        List<Seat> allSeats = seatRepository.findByVenueId(event.getVenue().getId()); // Needs findByVenueId in SeatRepository
        
        // Confirmed bookings stay unavailable even after their temporary lock is released.
        List<Long> bookedSeatIds = bookingItemRepository.findConfirmedSeatIdsByEventId(eventId);
        
        return allSeats.stream().map(seat -> {
            boolean available = true;
            if (bookedSeatIds.contains(seat.getId())) {
                available = false;
            } else {
                // Check Redis lock
                String lockKey = "event:" + eventId + ":seat:" + seat.getId();
                if (seatLockService.isSeatLocked(eventId, seat.getId())) {
                    available = false;
                }
            }
            
            return SeatResponse.builder()
                    .id(seat.getId())
                    .seatNumber(seat.getSeatNumber())
                    .row(seat.getRow())
                    .section(seat.getSection())
                    .category(seat.getCategory())
                    .available(available)
                    .build();
        }).collect(Collectors.toList());
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .imageUrl(event.getImageUrl())
                .venue(VenueResponse.builder()
                        .id(event.getVenue().getId())
                        .name(event.getVenue().getName())
                        .address(event.getVenue().getAddress())
                        .city(event.getVenue().getCity())
                        .state(event.getVenue().getState())
                        .capacity(event.getVenue().getCapacity())
                        .description(event.getVenue().getDescription())
                        .build())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .bookingOpenTime(event.getBookingOpenTime())
                .bookingCloseTime(event.getBookingCloseTime())
                .status(event.getStatus())
                .organizerId(event.getOrganizer() != null ? event.getOrganizer().getId() : null)
                .organizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : null)
                .ticketCategories(event.getTicketCategories() != null ? event.getTicketCategories().stream()
                        .map(tc -> TicketCategoryResponse.builder()
                                .id(tc.getId())
                                .name(tc.getName())
                                .price(tc.getPrice())
                                .totalSeats(tc.getTotalSeats())
                                .availableSeats(tc.getAvailableSeats())
                                .seatCategory(tc.getSeatCategory())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private EventSummaryResponse mapToSummary(Event event) {
        BigDecimal startingPrice = event.getTicketCategories() != null ? 
            event.getTicketCategories().stream()
                .map(TicketCategory::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO) : BigDecimal.ZERO;
                
        int totalSeats = event.getTicketCategories() != null ? 
            event.getTicketCategories().stream().mapToInt(TicketCategory::getTotalSeats).sum() : 0;
            
        int availableSeats = event.getTicketCategories() != null ? 
            event.getTicketCategories().stream().mapToInt(TicketCategory::getAvailableSeats).sum() : 0;

        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .category(event.getCategory())
                .imageUrl(event.getImageUrl())
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .city(event.getVenue() != null ? event.getVenue().getCity() : null)
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .status(event.getStatus())
                .organizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : null)
                .startingPrice(startingPrice)
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .build();
    }

    private void assertCanManage(Event event, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (actor.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (event.getOrganizer() == null || !event.getOrganizer().getId().equals(actorId)) {
            throw new UnauthorizedException("You can only manage events that you posted");
        }
    }
}


