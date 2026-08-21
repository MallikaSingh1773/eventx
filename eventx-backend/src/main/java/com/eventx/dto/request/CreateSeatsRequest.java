package com.eventx.dto.request;

import com.eventx.entity.enums.SeatCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeatsRequest {
    @NotNull
    private Long venueId;

    @NotNull
    private SeatCategory category;

    private String section;

    @NotBlank
    private String rowPrefix;

    private int startNumber;
    private int endNumber;
}
