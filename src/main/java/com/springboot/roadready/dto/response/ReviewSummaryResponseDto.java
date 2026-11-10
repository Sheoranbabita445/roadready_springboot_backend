package com.springboot.roadready.dto.response;

// average rating (one decimal, null when there are no reviews) and number of reviews of one car
public record ReviewSummaryResponseDto(
        Long carId,
        Double averageRating,
        Long totalReviews
) {
}
