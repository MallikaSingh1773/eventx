package com.eventx.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatsRequest {
    @NotNull(message = "Event ID cannot be null")
    private Long eventId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<SeatSelectionRequest> seats;
}
