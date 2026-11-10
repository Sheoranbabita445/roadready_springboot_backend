
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.LocationRequestDto;
import com.springboot.roadready.dto.response.LocationResponseDto;
import com.springboot.roadready.model.Location;
import com.springboot.roadready.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private LocationRepository locationRepository;


    // Test 1: Add location successfully
    @Test
    public void addLocationTest() {

        LocationRequestDto dto = new LocationRequestDto(
                "Mumbai",
                "Maharashtra",
                "Navi Mumbai"
        );

        Location location = new Location(
                1L,
                "Mumbai",
                "Maharashtra",
                "Navi Mumbai"
        );

        when(locationRepository.save(any(Location.class)))
                .thenReturn(location);

        LocationResponseDto result = locationService.addLocation(dto);

        assertEquals(1L, result.id());
        assertEquals("Mumbai", result.city());
        assertEquals("Maharashtra", result.state());
        assertEquals("Navi Mumbai", result.address());

        verify(locationRepository, times(1)).save(any(Location.class));
        verifyNoMoreInteractions(locationRepository);
    }


    // Test 2: Check location details after adding
    @Test
    public void addLocationTest_Details() {

        LocationRequestDto dto = new LocationRequestDto(
                "Pune",
                "Maharashtra",
                "Hinjewadi"
        );

        Location location = new Location();
        location.setId(2L);
        location.setCity("Pune");
        location.setState("Maharashtra");
        location.setAddress("Hinjewadi");

        when(locationRepository.save(any(Location.class)))
                .thenReturn(location);

        LocationResponseDto result = locationService.addLocation(dto);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Pune", result.city());
        assertEquals("Hinjewadi", result.address());

        verify(locationRepository, times(1)).save(any(Location.class));
        verifyNoMoreInteractions(locationRepository);
    }


    // Test 3: Get all locations
    @Test
    public void getAllLocationsTest() {

        Location location1 = new Location(
                1L,
                "Mumbai",
                "Maharashtra",
                "Navi Mumbai"
        );

        Location location2 = new Location(
                2L,
                "Pune",
                "Maharashtra",
                "Hinjewadi"
        );

        when(locationRepository.getAllLocationsSorted())
                .thenReturn(List.of(location1, location2));

        List<LocationResponseDto> result = locationService.getAllLocations();

        assertEquals(2, result.size());
        assertEquals("Mumbai", result.get(0).city());
        assertEquals("Pune", result.get(1).city());

        verify(locationRepository, times(1)).getAllLocationsSorted();
        verifyNoMoreInteractions(locationRepository);
    }


    // Test 4: Get all locations when no locations exist
    @Test
    public void getAllLocationsTest_Empty() {

        when(locationRepository.getAllLocationsSorted())
                .thenReturn(List.of());

        List<LocationResponseDto> result = locationService.getAllLocations();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(locationRepository, times(1)).getAllLocationsSorted();
        verifyNoMoreInteractions(locationRepository);
    }


    // Test 5: Check all fields of returned location DTO
    @Test
    public void getAllLocationsTest_Details() {

        Location location = new Location(
                3L,
                "Thane",
                "Maharashtra",
                "Ghodbunder Road"
        );

        when(locationRepository.getAllLocationsSorted())
                .thenReturn(List.of(location));

        List<LocationResponseDto> result = locationService.getAllLocations();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).id());
        assertEquals("Thane", result.get(0).city());
        assertEquals("Maharashtra", result.get(0).state());
        assertEquals("Ghodbunder Road", result.get(0).address());

        verify(locationRepository, times(1)).getAllLocationsSorted();
        verifyNoMoreInteractions(locationRepository);
    }
}
