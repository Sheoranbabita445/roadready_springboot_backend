package com.springboot.roadready.dto.request;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

// DTO to create a reservation
public record ReservationRequestDto(

        @NotNull(message = "Car ID is required")
        Long carId,

        @NotNull(message = "Pickup date and time is required")
        LocalDateTime pickupAt,

        @NotNull(message = "Drop-off date and time is required")
        LocalDateTime dropoffAt,

        String promoCode

) {}