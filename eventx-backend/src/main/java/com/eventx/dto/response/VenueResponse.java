package com.eventx.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private int capacity;
    private String description;
}
