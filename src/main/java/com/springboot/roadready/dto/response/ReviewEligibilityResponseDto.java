package com.springboot.roadready.dto.response;

// Can the logged-in customer review this car right now?
// reservationId is the completed, not yet reviewed reservation the review will be attached to.
public record ReviewEligibilityResponseDto(
        boolean eligible,
        Long reservationId,
        String message
) {
}
