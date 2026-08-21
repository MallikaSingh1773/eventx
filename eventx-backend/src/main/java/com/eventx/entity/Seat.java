package com.eventx.entity;

import com.eventx.entity.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", 
    indexes = {@Index(name = "idx_seat_venue", columnList = "venue_id")}, 
    uniqueConstraints = {@UniqueConstraint(columnNames = {"venue_id", "seat_number", "row_name", "section"})}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber;

    @Column(name = "row_name")
    private String row;

    private String section;

    @Enumerated(EnumType.STRING)
    private SeatCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @ToString.Exclude
    private Venue venue;
}
