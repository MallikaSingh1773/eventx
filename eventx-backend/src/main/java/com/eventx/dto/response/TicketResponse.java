package com.eventx.dto.response;

import com.eventx.entity.enums.SeatCategory;
import com.eventx.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private Long bookingId;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    
    private String venueName;
    private String seatNumber;
    private String row;
    private String section;
    private SeatCategory seatCategory;
    
    private String ticketCategoryName;
    private BigDecimal price;
    
    private String ticketCode;
    private String qrCodeData;
    private TicketStatus status;
    
    private String attendeeName;
    private String attendeeEmail;
    
    private LocalDateTime createdAt;
}
