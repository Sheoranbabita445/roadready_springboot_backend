package com.springboot.roadready.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record CarRequestDto(

        @NotNull(message = "Brand ID is required")
        Long brandId,

        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Rental agent ID is required")
        Long rentalAgentId,

        @NotBlank(message = "Registration number is required")
        String registrationNumber,

        @NotBlank(message = "Model is required")
        String model,

        @NotNull(message = "Manufacture year is required")
        @Min(value = 1886, message = "Manufacture year must be valid")
        Integer manufactureYear,

        @NotBlank(message = "Vehicle type is required")
        String vehicleType,

        @NotBlank(message = "Fuel type is required")
        String fuelType,

        @NotBlank(message = "Transmission is required")
        String transmission,

        @NotNull(message = "Number of seats is required")
        @Positive(message = "Number of seats must be greater than zero")
        Integer seats,

        @NotNull(message = "Price per day is required")
        @Positive(message = "Price per day must be greater than zero")
        BigDecimal pricePerDay,

        @NotBlank(message = "Status is required")
        String status,

        @Size(max = 500, message = "Image name cannot exceed 500 characters")
        String imageUrl,

        @Size(max = 1000, message = "Maintenance note cannot exceed 1000 characters")
        String maintenanceNote
) {
}