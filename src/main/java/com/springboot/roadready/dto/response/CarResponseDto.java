package com.springboot.roadready.dto.response;

import com.springboot.roadready.enums.CarStatus;
import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        Long brandId,
        String brandName,
        Long locationId,
        String city,
        Long rentalAgentId,
        String agentUsername,
        String registrationNumber,
        String model,
        Integer manufactureYear,
        String vehicleType,
        String fuelType,
        String transmission,
        Integer seats,
        BigDecimal pricePerDay,
        CarStatus status,
        String imageUrl,
        String maintenanceNote
) {
}
