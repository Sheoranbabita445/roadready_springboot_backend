package com.springboot.roadready.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminRequestDto(

        @NotBlank(message = "Name is required")
        @NotNull(message = "Name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        @NotNull(message = "Phone number is required")
        String phone,

        @NotNull(message = "Username is required")
        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password

) {
}