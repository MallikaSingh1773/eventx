package com.eventx.entity;

import com.eventx.entity.enums.EventCategory;
import com.eventx.entity.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_date", columnList = "event_date"), 
    @Index(name = "idx_event_status", columnList = "status"), 
    @Index(name = "idx_event_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private EventCategory category;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @ToString.Exclude
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    @ToString.Exclude
    private User organizer;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;
    
    private LocalDateTime bookingOpenTime;
    private LocalDateTime bookingCloseTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatus status = EventStatus.PUBLISHED;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TicketCategory> ticketCategories;

    @OneToMany(mappedBy = "event")
    @ToString.Exclude
    private List<Booking> bookings;
}
