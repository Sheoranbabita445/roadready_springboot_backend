package com.springboot.roadready.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RentalAgentRequestDto(

        @NotBlank(message = "Employee code is required")
        @NotNull(message = "Employee code is required")
        String employeeCode,

        @NotNull(message = "Username is required")
        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password

) {
}
