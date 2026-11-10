package com.springboot.roadready.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO to add a location
public record LocationRequestDto(

        @NotBlank(message = "City is required")
        @NotNull(message = "City cannot be null")
        String city,

        @NotBlank(message = "State is required")
        @NotNull(message = "State cannot be null")
        String state,

        @NotBlank(message = "Address is required")
        @NotNull(message = "Address cannot be null")
        String address

) {}