package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.ReviewRequestDto;
import com.springboot.roadready.dto.response.ReviewEligibilityResponseDto;
import com.springboot.roadready.dto.response.ReviewFilterDto;
import com.springboot.roadready.dto.response.ReviewResponseDto;
import com.springboot.roadready.dto.response.ReviewSummaryResponseDto;
import com.springboot.roadready.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewService reviewService;

    // to add a review for a completed reservation (customer only)
    @PostMapping("/api/review/add")
    public ReviewResponseDto addReview(
            @Valid @RequestBody ReviewRequestDto dto,
            Principal principal) {

        return reviewService.addReview(dto, principal.getName());
    }


    // to get the reviews of a car (everyone can read them)
    @GetMapping("/api/review/car/{carId}")
    public ReviewFilterDto getReviewsByCarId(
            @PathVariable Long carId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return reviewService.getReviewsByCarId(carId, page, size);
    }


    // rating and reviews of a car
    @GetMapping("/api/review/car/{carId}/summary")
    public ReviewSummaryResponseDto getReviewSummary(@PathVariable Long carId) {
        return reviewService.getReviewSummary(carId);
    }


    // only logged-in customer can review the car and who has completed the reservation
    @GetMapping("/api/review/car/{carId}/eligibility")
    public ReviewEligibilityResponseDto checkEligibility(
            @PathVariable Long carId,
            Principal principal) {

        return reviewService.checkEligibility(carId, principal.getName());
    }

}
