
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.ReviewRequestDto;
import com.springboot.roadready.dto.response.PaginationDto;
import com.springboot.roadready.dto.response.ReviewEligibilityResponseDto;
import com.springboot.roadready.dto.response.ReviewFilterDto;
import com.springboot.roadready.dto.response.ReviewResponseDto;
import com.springboot.roadready.dto.response.ReviewSummaryResponseDto;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.OwnershipException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.mapper.ReviewMapper;
import com.springboot.roadready.model.Reservation;
import com.springboot.roadready.model.Review;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.ReservationRepository;
import com.springboot.roadready.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReservationRepository reservationRepository,
            CarRepository carRepository) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.carRepository = carRepository;
    }

    // add a review for a completed reservation.
    // The review belongs to the reservation, and through it to the car and the customer.
    @Transactional
    public ReviewResponseDto addReview(
            ReviewRequestDto dto,
            String customerUsername) {

        log.info("Adding review for reservation ID: {} by customer: {}",
                dto.reservationId(), customerUsername);

        // Find the reservation (locked, so two clicks cannot create two reviews)
        Reservation reservation = reservationRepository.findByIdForUpdate(dto.reservationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reservation not found"));

        // Check whether the reservation belongs to the logged-in customer
        if (!reservation.getCustomer().getUser().getUsername()
                .equals(customerUsername)) {
            throw new OwnershipException(
                    "You can review only your own reservations");
        }

        // Check whether the reservation is completed
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new InvalidCallException(
                    "Only completed reservations can be reviewed");
        }

        // Check whether a review already exists for this reservation
        Long reviewCount =
                reviewRepository.countReviewsByReservationId(reservation.getId());

        if (reviewCount > 0) {
            throw new InvalidCallException(
                    "A review already exists for this reservation");
        }

        // Create the review
        Review review = new Review();
        review.setReservation(reservation);
        review.setRating(dto.rating());
        review.setComment(dto.comment().trim());
        review.setReviewDate(LocalDateTime.now());

        // Save the review
        Review savedReview = reviewRepository.save(review);

        log.info("Review successfully added for reservation ID: {}, review ID: {}",
                reservation.getId(), savedReview.getId());

        // Convert the saved review to response DTO
        return ReviewMapper.convertEntityToDto(savedReview);
    }


    // get the reviews of a car, newest first
    public ReviewFilterDto getReviewsByCarId(
            Long carId, Integer page, Integer size) {

        log.info("Fetching reviews for car ID: {}, page: {}, size: {}",
                carId, page, size);

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // the car must exist
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found");
        }

        Page<ReviewResponseDto> reviewPage = reviewRepository.getReviewsByCarId(carId, pageable);

        log.info("Successfully fetched {} reviews for car ID: {}",
                reviewPage.getContent().size(), carId);

        return new ReviewFilterDto(
                reviewPage.getContent(),
                new PaginationDto(reviewPage.getTotalPages(), reviewPage.getTotalElements(), reviewPage.getContent().size())
        );
    }


    // get the rating and reviews of a car
    public ReviewSummaryResponseDto getReviewSummary(Long carId) {

        log.info("Fetching review summary for car ID: {}", carId);

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found");
        }

        Double average = reviewRepository.getAverageRatingByCarId(carId);
        Long total = reviewRepository.countReviewsByCarId(carId);

        // round the average to one decimal
        if (average != null) {
            average = Math.round(average * 10.0) / 10.0;
        }

        log.info("Successfully fetched review summary for car ID: {}, average rating: {}, total reviews: {}",
                carId, average, total);

        return new ReviewSummaryResponseDto(carId, average, total);
    }


    // check whether the logged-in customer may review this car now
    // they need a completed reservation of this car that has no review yet.
    public ReviewEligibilityResponseDto checkEligibility(Long carId, String customerUsername) {

        log.info("Checking review eligibility for car ID: {} and customer: {}",
                carId, customerUsername);

        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found");
        }

        List<Long> reviewable = reviewRepository.getReviewableReservationIds(
                carId, customerUsername, ReservationStatus.COMPLETED);

        if (!reviewable.isEmpty()) {

            log.info("Customer {} is eligible to review car ID: {}",
                    customerUsername, carId);

            return new ReviewEligibilityResponseDto(
                    true,
                    reviewable.get(0),
                    "You completed a rental of this car. Share how it went.");
        }

        Long completed = reviewRepository.countCompletedReservationsForCar(
                carId, customerUsername, ReservationStatus.COMPLETED);

        if (completed > 0) {

            log.info("Customer {} has already reviewed all completed rentals for car ID: {}",
                    customerUsername, carId);

            return new ReviewEligibilityResponseDto(
                    false,
                    null,
                    "You have already reviewed every rental you completed with this car.");
        }

        log.info("Customer {} is not eligible to review car ID: {}",
                customerUsername, carId);

        return new ReviewEligibilityResponseDto(
                false,
                null,
                "Only customers who have completed a rental of this car can review it.");
    }
}
