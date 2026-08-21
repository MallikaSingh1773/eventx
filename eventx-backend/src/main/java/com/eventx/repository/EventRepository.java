package com.eventx.repository;

import com.eventx.entity.Event;
import com.eventx.entity.enums.EventCategory;
import com.eventx.entity.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);
    
    Page<Event> findByStatusAndCategoryAndEventDateAfter(EventStatus status, EventCategory category, LocalDate date, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.eventDate >= :today ORDER BY e.eventDate ASC")
    Page<Event> findUpcoming(@Param("today") LocalDate today, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Event> search(@Param("query") String query, Pageable pageable);
    
    long countByStatus(EventStatus status);

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);
}
