package com.eventx.repository;

import com.eventx.entity.Seat;
import com.eventx.entity.enums.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenueId(Long venueId);
    List<Seat> findByVenueIdAndCategory(Long venueId, SeatCategory category);
    List<Seat> findByIdIn(List<Long> ids);
}
