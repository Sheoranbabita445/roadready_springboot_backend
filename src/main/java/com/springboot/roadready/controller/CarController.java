package com.springboot.roadready.controller;


import com.springboot.roadready.dto.request.CarRequestDto;
import com.springboot.roadready.dto.response.CarFilterDto;
import com.springboot.roadready.dto.response.CarResponseDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("api/car")

public class CarController {
    private final CarService carService;


    // add a car
    @PostMapping("/add")
    public Long addCar(@Valid @RequestBody CarRequestDto carRequestDto)
    {
        return carService.addCar(carRequestDto);
    }


    // upload the picture of a car
    @PostMapping("/{id}/upload")
    public CarResponseDto uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return carService.uploadImage(id, file);
    }


    // get all cars
    @GetMapping("/all")
    public CarFilterDto getAllCars(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        return carService.getAllCars(page, size);
    }

    // get a car by its ID (includes brandId, locationId and rentalAgentId for the edit form)
    @GetMapping("/{id}")
    public CarResponseDto getCarById(@PathVariable Long id)
    {
        return carService.getCarById(id);
    }


    // update car details
    @PutMapping("/{id}")
    public void updateCar(@PathVariable Long id, @Valid @RequestBody CarRequestDto carRequestDto) {
        carService.updateCar(id, carRequestDto);
    }


    // filter cars (all filters optional and combinable)
    @GetMapping("/filter")
    public CarFilterDto dynamicFilterCar(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return carService.dynamicFilterCar(
                brandId,
                locationId,
                fuelType,
                transmission,
                vehicleType,
                minPrice,
                maxPrice,
                status,
                search,
                page,
                size);
    }


    // get cars assigned to the logged-in rental agent
    @GetMapping("/api/rental-agent/my-cars")
    public CarFilterDto getMyCars(
            Principal principal,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return carService.getCarsByRentalAgentUsername(
                principal.getName(), status, search, page, size);
    }


    // update the status of a car assigned to the logged-in rental agent
    @PutMapping("/api/rental-agent/car/{carId}/status")
    public String updateCarStatusByRentalAgent(
            @PathVariable Long carId,
            @RequestParam CarStatus status,
            Principal principal) {

        return carService.updateCarStatusByRentalAgent(
                carId,
                status,
                principal.getName());
    }


    // update maintenance details for a car assigned to a rental agent
    @PutMapping("/api/rental-agent/car/{carId}/maintenance")
    public void updateMaintenanceNoteByRentalAgent(
            @PathVariable Long carId,
            @RequestParam String maintenanceNote,
            Principal principal) {

        carService.updateMaintenanceNoteByRentalAgent(
                carId,
                maintenanceNote,
                principal.getName());
    }

}
