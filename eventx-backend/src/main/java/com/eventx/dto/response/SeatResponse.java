package com.eventx.dto.response;

import com.eventx.entity.enums.SeatCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatResponse {
    private Long id;
    private String seatNumber;
    private String row;
    private String section;
    private SeatCategory category;
    private boolean available;
}
