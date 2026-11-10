package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.response.ReviewResponseDto;
import com.springboot.roadready.model.Customer;
import com.springboot.roadready.model.Reservation;
import com.springboot.roadready.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    // convert Review entity to DTO
    public static ReviewResponseDto convertEntityToDto(Review review) {
        Reservation reservation = review.getReservation();
        Customer customer = reservation.getCustomer();

        return new ReviewResponseDto(
                review.getId(),
                reservation.getId(),
                reservation.getCar().getId(),
                customer.getUser().getUsername(),
                customer.getFirstName() + " " + customer.getLastName(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate()
        );
    }
}
