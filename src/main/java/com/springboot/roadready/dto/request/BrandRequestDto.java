package com.springboot.roadready.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO to add a brand
public record BrandRequestDto(
        @NotBlank(message = "Brand name cannot be blank")
        @NotNull(message = "Brand name is required")
        String name
) {}