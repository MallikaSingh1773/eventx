package com.eventx.service;

import com.eventx.dto.request.CreateEventRequest;
import com.eventx.dto.request.UpdateEventRequest;
import com.eventx.dto.response.EventResponse;
import com.eventx.dto.response.EventSummaryResponse;
import com.eventx.dto.response.SeatResponse;
import com.eventx.entity.enums.EventCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface EventService {
    EventResponse createEvent(CreateEventRequest request, Long organizerId);
    EventResponse updateEvent(Long id, UpdateEventRequest request, Long actorId);
    void deleteEvent(Long id, Long actorId);
    EventResponse publishEvent(Long id, Long actorId);
    EventResponse getEvent(Long id);
    Page<EventSummaryResponse> getAllEvents(Pageable pageable);
    Page<EventSummaryResponse> getEventsByOrganizer(Long organizerId, Pageable pageable);
    Page<EventSummaryResponse> getPublishedEvents(Pageable pageable);
    Page<EventSummaryResponse> searchEvents(String query, EventCategory category, String city, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
    Page<EventSummaryResponse> getUpcomingEvents(Pageable pageable);
    List<SeatResponse> getAvailableSeats(Long eventId);
}
