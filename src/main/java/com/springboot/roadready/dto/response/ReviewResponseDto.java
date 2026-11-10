package com.springboot.roadready.dto.response;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long id,
        Long reservationId,
        Long carId,
        String customerUsername,
        String customerName,
        Integer rating,
        String comment,
        LocalDateTime reviewDate
) {
}
