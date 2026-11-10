package com.springboot.roadready.dto.response;

import java.time.LocalDateTime;

public record CarAvailabilityResponseDto(
        Long carId,
        LocalDateTime pickupAt,
        LocalDateTime dropoffAt,
        String status,
        String message
) {
}