
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.RentalAgentRequestDto;
import com.springboot.roadready.dto.response.RentalAgentDashboardSummaryDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.model.RentalAgent;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import com.springboot.roadready.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalAgentServiceTest {

    @InjectMocks
    private RentalAgentService rentalAgentService;

    @Mock
    private RentalAgentRepository rentalAgentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CarRepository carRepository;

    @Mock
    private ReservationService reservationService;


    // Test 1: Add rental agent successfully
    @Test
    public void addRentalAgentTest() {

        RentalAgentRequestDto dto = new RentalAgentRequestDto(
                "EMP101",
                "agent1",
                "password123"
        );

        User user = new User();
        user.setUsername("agent1");
        user.setPassword("encodedPassword");
        user.setRole(Role.RENTAL_AGENT);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        rentalAgentService.addRentalAgent(dto);

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(rentalAgentRepository, times(1)).save(any(RentalAgent.class));
    }


    // Test 2: Get dashboard summary with cars and reservations
    @Test
    public void getDashboardSummaryTest() {

        when(carRepository.countCarsByStatusForAgent("agent1"))
                .thenReturn(List.<Object[]>of(
                        new Object[]{CarStatus.AVAILABLE, 5L},
                        new Object[]{CarStatus.BOOKED, 3L},
                        new Object[]{CarStatus.RENTED, 2L}
                ));

        when(reservationService.countReservationsByStatusForAgent("agent1"))
                .thenReturn(Map.of(
                        "CONFIRMED", 4L,
                        "PENDING", 2L,
                        "CANCELLED", 1L
                ));

        RentalAgentDashboardSummaryDto result =
                rentalAgentService.getDashboardSummary("agent1");

        assertEquals(10L, result.assignedCars());
        assertEquals(7L, result.totalReservations());

        assertEquals(5L, result.carsByStatus().get("AVAILABLE"));
        assertEquals(3L, result.carsByStatus().get("BOOKED"));
        assertEquals(2L, result.carsByStatus().get("RENTED"));

        verify(carRepository, times(1))
                .countCarsByStatusForAgent("agent1");

        verify(reservationService, times(1))
                .countReservationsByStatusForAgent("agent1");
    }


    // Test 3: Get dashboard summary when there are no cars
    @Test
    public void getDashboardSummaryTest_NoCars() {

        when(carRepository.countCarsByStatusForAgent("agent1"))
                .thenReturn(List.<Object[]>of());

        when(reservationService.countReservationsByStatusForAgent("agent1"))
                .thenReturn(Map.of());

        RentalAgentDashboardSummaryDto result =
                rentalAgentService.getDashboardSummary("agent1");

        assertEquals(0L, result.assignedCars());
        assertEquals(0L, result.totalReservations());

        assertEquals(0L, result.carsByStatus().get("AVAILABLE"));
        assertEquals(0L, result.carsByStatus().get("BOOKED"));
        assertEquals(0L, result.carsByStatus().get("RENTED"));
        assertEquals(0L, result.carsByStatus().get("MAINTENANCE"));

        verify(carRepository, times(1))
                .countCarsByStatusForAgent("agent1");

        verify(reservationService, times(1))
                .countReservationsByStatusForAgent("agent1");
    }


    // Test 4: Get dashboard summary when only available cars exist
    @Test
    public void getDashboardSummaryTest_AvailableCars() {

        when(carRepository.countCarsByStatusForAgent("agent1"))
                .thenReturn(List.<Object[]>of(
                        new Object[]{CarStatus.AVAILABLE, 4L}
                ));

        when(reservationService.countReservationsByStatusForAgent("agent1"))
                .thenReturn(Map.of());

        RentalAgentDashboardSummaryDto result =
                rentalAgentService.getDashboardSummary("agent1");

        assertEquals(4L, result.assignedCars());
        assertEquals(0L, result.totalReservations());

        assertEquals(4L, result.carsByStatus().get("AVAILABLE"));
        assertEquals(0L, result.carsByStatus().get("BOOKED"));
        assertEquals(0L, result.carsByStatus().get("RENTED"));
        assertEquals(0L, result.carsByStatus().get("MAINTENANCE"));

        verify(carRepository, times(1))
                .countCarsByStatusForAgent("agent1");

        verify(reservationService, times(1))
                .countReservationsByStatusForAgent("agent1");
    }


    // Test 5: Calculate total reservations for all statuses
    @Test
    public void getDashboardSummaryTest_Reservations() {

        when(carRepository.countCarsByStatusForAgent("agent1"))
                .thenReturn(List.<Object[]>of(
                        new Object[]{CarStatus.AVAILABLE, 2L}
                ));

        when(reservationService.countReservationsByStatusForAgent("agent1"))
                .thenReturn(Map.of(
                        "CONFIRMED", 5L,
                        "PENDING", 3L,
                        "CANCELLED", 2L
                ));

        RentalAgentDashboardSummaryDto result =
                rentalAgentService.getDashboardSummary("agent1");

        assertEquals(2L, result.assignedCars());
        assertEquals(10L, result.totalReservations());

        assertEquals(5L, result.reservationsByStatus().get("CONFIRMED"));
        assertEquals(3L, result.reservationsByStatus().get("PENDING"));
        assertEquals(2L, result.reservationsByStatus().get("CANCELLED"));

        verify(carRepository, times(1))
                .countCarsByStatusForAgent("agent1");

        verify(reservationService, times(1))
                .countReservationsByStatusForAgent("agent1");
    }
}
