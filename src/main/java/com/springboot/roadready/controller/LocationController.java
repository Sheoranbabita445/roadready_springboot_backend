package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.LocationRequestDto;
import com.springboot.roadready.dto.response.LocationResponseDto;
import com.springboot.roadready.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("api/location")
public class LocationController {

    private final LocationService locationService;

    // add a location
    @PostMapping("/add")
    public LocationResponseDto addLocation(@Valid @RequestBody LocationRequestDto locationRequestDto) {
        return locationService.addLocation(locationRequestDto);
    }

    // get all locations (dropdowns and the Locations page)
    @GetMapping("/all")
    public List<LocationResponseDto> getAllLocations() {
        return locationService.getAllLocations();
    }
}
