package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.request.CarRequestDto;
import com.springboot.roadready.dto.response.CarResponseDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.model.Car;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {

    // convert DTO to Car entity
    public static Car convertDtoToEntity(CarRequestDto dto) {

        Car car = new Car();

        car.setRegistrationNumber(dto.registrationNumber());
        car.setModel(dto.model());
        car.setManufactureYear(dto.manufactureYear());
        car.setVehicleType(dto.vehicleType());
        car.setFuelType(dto.fuelType());
        car.setTransmission(dto.transmission());
        car.setSeats(dto.seats());
        car.setPricePerDay(dto.pricePerDay());
        car.setStatus(CarStatus.valueOf(dto.status().toUpperCase()));
        car.setImageUrl(blankToNull(dto.imageUrl()));
        car.setMaintenanceNote(dto.maintenanceNote());

        return car;
    }

    // convert Car entity to response DTO
    public static CarResponseDto convertEntityToDto(Car car) {

        return new CarResponseDto(
                car.getId(),
                car.getBrand().getId(),
                car.getBrand().getName(),
                car.getLocation().getId(),
                car.getLocation().getCity(),
                car.getRentalAgent().getId(),
                car.getRentalAgent().getUser().getUsername(),
                car.getRegistrationNumber(),
                car.getModel(),
                car.getManufactureYear(),
                car.getVehicleType(),
                car.getFuelType(),
                car.getTransmission(),
                car.getSeats(),
                car.getPricePerDay(),
                car.getStatus(),
                car.getImageUrl(),
                car.getMaintenanceNote()
        );
    }

    // Method to update Car entity from DTO
    public static void updateEntityFromDto(CarRequestDto dto, Car car) {

        car.setRegistrationNumber(dto.registrationNumber());
        car.setModel(dto.model());
        car.setManufactureYear(dto.manufactureYear());
        car.setVehicleType(dto.vehicleType());
        car.setFuelType(dto.fuelType());
        car.setTransmission(dto.transmission());
        car.setSeats(dto.seats());
        car.setPricePerDay(dto.pricePerDay());
        car.setStatus(CarStatus.valueOf(dto.status().toUpperCase()));
        if (dto.imageUrl() != null && !dto.imageUrl().isBlank()) {
            car.setImageUrl(dto.imageUrl().trim());
        }
        car.setMaintenanceNote(dto.maintenanceNote());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
