package com.springboot.roadready.dto.request;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

// modify a reservation
public record ModifyReservationRequestDto(

        @NotNull(message = "Pickup date and time is required")
        LocalDateTime pickupAt,

        @NotNull(message = "Drop-off date and time is required")
        LocalDateTime dropoffAt

) {}