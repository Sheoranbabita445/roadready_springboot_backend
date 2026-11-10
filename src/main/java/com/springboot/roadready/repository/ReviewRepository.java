package com.springboot.roadready.repository;

import com.springboot.roadready.dto.response.ReviewResponseDto;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // count reviews for a reservation
    @Query("""
            SELECT COUNT(rv)
            FROM Review rv
            WHERE rv.reservation.id = ?1
            """)
    Long countReviewsByReservationId(Long reservationId);


    // Reviews of one car with the customer's name, newest first
    @Query("""
            SELECT new com.springboot.roadready.dto.response.ReviewResponseDto(
                rv.id, r.id, car.id, u.username,
                CONCAT(c.firstName, ' ', c.lastName),
                rv.rating, rv.comment, rv.reviewDate
            )
            FROM Review rv
            JOIN rv.reservation r
            JOIN r.car car
            JOIN r.customer c
            JOIN c.user u
            WHERE car.id = ?1
            ORDER BY rv.reviewDate DESC, rv.id DESC
            """)
    Page<ReviewResponseDto> getReviewsByCarId(
            Long carId,
            Pageable pageable
    );


    // rating of a car (null when it has no reviews)
    @Query("""
            SELECT AVG(rv.rating)
            FROM Review rv
            WHERE rv.reservation.car.id = ?1
            """)
    Double getAverageRatingByCarId(Long carId);


    // Number of reviews of a car
    @Query("""
            SELECT COUNT(rv)
            FROM Review rv
            WHERE rv.reservation.car.id = ?1
            """)
    Long countReviewsByCarId(Long carId);


    // Completed reservations of this customer for this car that have no review yet (oldest first).
    // A customer may review a car only through one of these.
    @Query("""
            SELECT r.id
            FROM Reservation r
            JOIN r.customer c
            JOIN c.user u
            WHERE r.car.id = ?1
            AND u.username = ?2
            AND r.status = ?3
            AND NOT EXISTS (
                SELECT rv.id FROM Review rv WHERE rv.reservation.id = r.id
            )
            ORDER BY r.id
            """)
    List<Long> getReviewableReservationIds(
            Long carId,
            String customerUsername,
            ReservationStatus completedStatus
    );


    // Does this customer have any completed reservation for this car? (to give a clear message)
    @Query("""
            SELECT COUNT(r)
            FROM Reservation r
            JOIN r.customer c
            JOIN c.user u
            WHERE r.car.id = ?1
            AND u.username = ?2
            AND r.status = ?3
            """)
    Long countCompletedReservationsForCar(
            Long carId,
            String customerUsername,
            ReservationStatus completedStatus
    );


    // Which of these reservations already have a review (marks "Reviewed" in My Reservations)
    @Query("""
            SELECT rv.reservation.id
            FROM Review rv
            WHERE rv.reservation.id IN ?1
            """)
    List<Long> getReviewedReservationIds(Collection<Long> reservationIds);

}
