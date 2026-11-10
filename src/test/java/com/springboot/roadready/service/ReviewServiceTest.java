
package com.springboot.roadready.service;

import com.springboot.roadready.dto.response.ReviewEligibilityResponseDto;
import com.springboot.roadready.dto.response.ReviewResponseDto;
import com.springboot.roadready.dto.response.ReviewSummaryResponseDto;
import com.springboot.roadready.dto.response.ReviewFilterDto;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.ReservationRepository;
import com.springboot.roadready.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CarRepository carRepository;

    @Test
    public void getReviewsByCarIdTest() {

        ReviewResponseDto responseDto = new ReviewResponseDto(
                1L,
                10L,
                5L,
                "customer1",
                "John Doe",
                5,
                "Excellent car",
                LocalDateTime.now()
        );

        Page<ReviewResponseDto> reviewPage =
                new PageImpl<>(List.of(responseDto));

        when(carRepository.existsById(5L)).thenReturn(true);

        when(reviewRepository.getReviewsByCarId(
                eq(5L), any(PageRequest.class)))
                .thenReturn(reviewPage);

        ReviewFilterDto result =
                reviewService.getReviewsByCarId(5L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.list().size());
        assertEquals("Excellent car", result.list().get(0).comment());
        assertEquals(1, result.paginationInfo().numRecordsInPage());

        verify(carRepository, times(1)).existsById(5L);

        verify(reviewRepository, times(1))
                .getReviewsByCarId(eq(5L), any(PageRequest.class));

        verifyNoMoreInteractions(carRepository, reviewRepository);
    }

    @Test
    public void getReviewsByCarIdInvalidPaginationTest() {

        assertThrows(InvalidCallException.class, () ->
                reviewService.getReviewsByCarId(5L, -1, 10));

        verifyNoInteractions(carRepository, reviewRepository);
    }

    @Test
    public void getReviewSummaryTest() {

        when(carRepository.existsById(5L)).thenReturn(true);
        when(reviewRepository.getAverageRatingByCarId(5L))
                .thenReturn(4.567);
        when(reviewRepository.countReviewsByCarId(5L))
                .thenReturn(10L);

        ReviewSummaryResponseDto result =
                reviewService.getReviewSummary(5L);

        assertNotNull(result);
        assertEquals(5L, result.carId());
        assertEquals(4.6, result.averageRating());
        assertEquals(10L, result.totalReviews());

        verify(carRepository, times(1)).existsById(5L);
        verify(reviewRepository, times(1)).getAverageRatingByCarId(5L);
        verify(reviewRepository, times(1)).countReviewsByCarId(5L);

        verifyNoMoreInteractions(carRepository, reviewRepository);
    }

    @Test
    public void checkEligibilityTest() {

        when(carRepository.existsById(5L)).thenReturn(true);

        when(reviewRepository.getReviewableReservationIds(
                5L, "customer1", ReservationStatus.COMPLETED))
                .thenReturn(List.of(10L));

        ReviewEligibilityResponseDto result =
                reviewService.checkEligibility(5L, "customer1");

        assertTrue(result.eligible());
        assertEquals(10L, result.reservationId());
        assertEquals(
                "You completed a rental of this car. Share how it went.",
                result.message()
        );

        verify(carRepository, times(1)).existsById(5L);

        verify(reviewRepository, times(1))
                .getReviewableReservationIds(
                        5L, "customer1", ReservationStatus.COMPLETED);

        verifyNoMoreInteractions(carRepository, reviewRepository);
    }

    @Test
    public void checkEligibilityAlreadyReviewedTest() {

        when(carRepository.existsById(5L)).thenReturn(true);

        when(reviewRepository.getReviewableReservationIds(
                5L, "customer1", ReservationStatus.COMPLETED))
                .thenReturn(List.of());

        when(reviewRepository.countCompletedReservationsForCar(
                5L, "customer1", ReservationStatus.COMPLETED))
                .thenReturn(2L);

        ReviewEligibilityResponseDto result =
                reviewService.checkEligibility(5L, "customer1");

        assertFalse(result.eligible());
        assertNull(result.reservationId());
        assertEquals(
                "You have already reviewed every rental you completed with this car.",
                result.message()
        );

        verify(carRepository, times(1)).existsById(5L);

        verify(reviewRepository, times(1))
                .getReviewableReservationIds(
                        5L, "customer1", ReservationStatus.COMPLETED);

        verify(reviewRepository, times(1))
                .countCompletedReservationsForCar(
                        5L, "customer1", ReservationStatus.COMPLETED);

        verifyNoMoreInteractions(carRepository, reviewRepository);
    }
}
