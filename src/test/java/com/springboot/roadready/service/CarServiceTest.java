
package com.springboot.roadready.service;

import com.springboot.roadready.dto.response.CarFilterDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.BrandRepository;
import com.springboot.roadready.repository.LocationRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RentalAgentRepository rentalAgentRepository;


    // Test 1: Get cars successfully with pagination
    @Test
    public void dynamicFilterCarTest() {

        when(carRepository.dynamicFilterCar(
                null, null, null, null, null,
                null, null, null, null,
                PageRequest.of(0, 5)
        )).thenReturn(new PageImpl<>(List.of()));

        CarFilterDto result = carService.getAllCars(0, 5);

        assertEquals(0, result.list().size());
        assertEquals(0, result.paginationInfo().totalPages());
        assertEquals(0L, result.paginationInfo().totalRecords());
        assertEquals(0, result.paginationInfo().numRecordsInPage());

        verify(carRepository, times(1))
                .dynamicFilterCar(
                        null, null, null, null, null,
                        null, null, null, null,
                        PageRequest.of(0, 5)
                );

        verifyNoMoreInteractions(carRepository);
    }


    // Test 2: Minimum price cannot be greater than maximum price
    @Test
    public void dynamicFilterCarTest_InvalidPrice() {

        assertThrows(InvalidCallException.class,
                () -> carService.dynamicFilterCar(
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("5000"),
                        new BigDecimal("2000"),
                        null,
                        null,
                        0,
                        5
                ));

        verifyNoInteractions(carRepository);
    }


    // Test 3: Get car by ID when car does not exist
    @Test
    public void getCarByIdTest_Invalid() {

        when(carRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> carService.getCarById(10L));

        verify(carRepository, times(1))
                .findById(10L);

        verifyNoMoreInteractions(carRepository);
    }


    // Test 4: Update car status by rental agent successfully
    @Test
    public void updateCarStatusByRentalAgentTest() {

        when(carRepository.updateCarStatusByRentalAgent(
                1L, "agent1", CarStatus.AVAILABLE
        )).thenReturn(1);

        String result = carService.updateCarStatusByRentalAgent(
                1L,
                CarStatus.AVAILABLE,
                "agent1"
        );

        assertEquals("Car status updated successfully", result);

        verify(carRepository, times(1))
                .updateCarStatusByRentalAgent(
                        1L, "agent1", CarStatus.AVAILABLE
                );

        verifyNoMoreInteractions(carRepository);
    }


    // Test 5: Update maintenance note when car is not assigned to rental agent
    @Test
    public void updateMaintenanceNoteByRentalAgentTest_Invalid() {

        when(carRepository.updateMaintenanceNoteByRentalAgent(
                1L, "agent1", "Engine repair required"
        )).thenReturn(0);

        assertThrows(ResourceNotFoundException.class,
                () -> carService.updateMaintenanceNoteByRentalAgent(
                        1L,
                        "Engine repair required",
                        "agent1"
                ));

        verify(carRepository, times(1))
                .updateMaintenanceNoteByRentalAgent(
                        1L, "agent1", "Engine repair required"
                );

        verifyNoMoreInteractions(carRepository);
    }

}
