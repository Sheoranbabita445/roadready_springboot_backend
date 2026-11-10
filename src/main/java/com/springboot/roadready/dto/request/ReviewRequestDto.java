package com.springboot.roadready.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDto(

        @NotNull(message = "Reservation ID is required")
        Long reservationId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @NotBlank(message = "Comment is required")
        @NotNull(message = "Comment is required")
        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String comment

) {
}
