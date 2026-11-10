
package com.springboot.roadready.service;

import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.model.Car;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.CustomerRepository;
import com.springboot.roadready.repository.PaymentRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import com.springboot.roadready.repository.ReservationRepository;
import com.springboot.roadready.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RentalAgentRepository rentalAgentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Test
    public void getMyReservationsInvalidPageTest() {

        assertThrows(InvalidCallException.class, () ->
                reservationService.getMyReservations(
                        "customer1", null, -1, 10));

        verifyNoInteractions(reservationRepository);
    }

    @Test
    public void getMyReservationsInvalidSizeTest() {

        assertThrows(InvalidCallException.class, () ->
                reservationService.getMyReservations(
                        "customer1", null, 0, 101));

        verifyNoInteractions(reservationRepository);
    }

    @Test
    public void countReservationsByStatusTest() {

        when(reservationRepository.countReservationsByStatus())
                .thenReturn(List.<Object[]>of(
                        new Object[]{ReservationStatus.PENDING, 5L},
                        new Object[]{ReservationStatus.CONFIRMED, 3L}
                ));

        Map<String, Long> result =
                reservationService.countReservationsByStatus();

        assertEquals(5L, result.get("PENDING"));
        assertEquals(3L, result.get("CONFIRMED"));
        assertEquals(0L, result.get("COMPLETED"));
        assertEquals(0L, result.get("CANCELLED"));

        verify(reservationRepository, times(1))
                .countReservationsByStatus();

        verifyNoMoreInteractions(reservationRepository);
    }

    @Test
    public void checkCarAvailabilityTest() {

        Long carId = 1L;

        LocalDateTime pickupAt = LocalDateTime.now().plusDays(5);
        LocalDateTime dropoffAt = pickupAt.plusDays(3);

        Car car = new Car();
        car.setStatus(CarStatus.AVAILABLE);

        when(carRepository.findById(carId))
                .thenReturn(Optional.of(car));

        when(reservationRepository.countOverlappingReservations(
                eq(carId), eq(pickupAt), eq(dropoffAt), anyList()))
                .thenReturn(0L);

        var result = reservationService.checkCarAvailability(
                carId, pickupAt, dropoffAt);

        assertEquals("AVAILABLE", result.status());
        assertEquals(carId, result.carId());

        verify(carRepository, times(1)).findById(carId);

        verify(reservationRepository, times(1))
                .countOverlappingReservations(
                        eq(carId), eq(pickupAt), eq(dropoffAt), anyList());

        verifyNoMoreInteractions(carRepository, reservationRepository);
    }

    @Test
    public void checkCarAvailabilityInvalidDateTest() {

        Long carId = 1L;

        LocalDateTime pickupAt = LocalDateTime.now().plusDays(5);
        LocalDateTime dropoffAt = pickupAt.minusDays(1);

        assertThrows(InvalidCallException.class, () ->
                reservationService.checkCarAvailability(
                        carId, pickupAt, dropoffAt));

        verifyNoInteractions(carRepository, reservationRepository);
    }
}
