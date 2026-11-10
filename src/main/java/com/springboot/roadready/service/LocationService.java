package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.LocationRequestDto;
import com.springboot.roadready.dto.response.LocationResponseDto;
import com.springboot.roadready.mapper.LocationMapper;
import com.springboot.roadready.model.Location;
import com.springboot.roadready.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;

    // add a location
    public LocationResponseDto addLocation(LocationRequestDto locationRequestDto) {

        log.info("Adding a new location");

        Location location = LocationMapper.convertDtoToEntity(locationRequestDto);
        Location savedLocation = locationRepository.save(location);

        log.info("Location added successfully with ID: {}", savedLocation.getId());

        return LocationMapper.convertEntityToDto(savedLocation);
    }

    // get all locations for the dropdowns and the Locations page
    public List<LocationResponseDto> getAllLocations() {

        log.info("Fetching all locations");

        List<LocationResponseDto> locations = locationRepository.getAllLocationsSorted()
                .stream()
                .map(LocationMapper::convertEntityToDto)
                .toList();

        log.info("Successfully fetched {} locations", locations.size());

        return locations;
    }
}