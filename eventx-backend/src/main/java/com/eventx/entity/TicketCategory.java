package com.eventx.entity;

import com.eventx.entity.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private int totalSeats;
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    private SeatCategory seatCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;
}
