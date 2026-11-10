package com.springboot.roadready.dto.response;

// used by the admin to pick an agent when adding or editing a car
public record RentalAgentResponseDto(
        Long id,
        String employeeCode,
        String username,
        boolean active
) {
}
