package com.eventx.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateVenueRequest {
    @NotBlank
    private String name;

    private String address;

    @NotBlank
    private String city;

    private String state;

    @Positive
    private int capacity;

    private String description;
}
