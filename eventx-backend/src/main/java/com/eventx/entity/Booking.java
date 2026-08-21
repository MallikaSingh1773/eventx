package com.eventx.entity;

import com.eventx.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_user", columnList = "user_id"), 
    @Index(name = "idx_booking_event", columnList = "event_id"), 
    @Index(name = "idx_booking_status", columnList = "status"), 
    @Index(name = "idx_booking_razorpay_order", columnList = "razorpay_order_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    private String razorpayOrderId;
    private LocalDateTime lockedUntil;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<BookingItem> bookingItems;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Payment payment;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "booking")
    @ToString.Exclude
    private List<Refund> refunds;
}
