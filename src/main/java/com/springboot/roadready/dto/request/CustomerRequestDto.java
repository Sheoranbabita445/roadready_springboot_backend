package com.springboot.roadready.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerRequestDto(

        @NotBlank(message = "First name is required")
        @NotNull(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        @NotNull(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @NotNull(message = "Email is required")
        @Email(message = "Email seems invalid")
        String email,

        @NotBlank(message = "Phone number is required")
        @NotNull(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Address is required")
        @NotNull(message = "Address is required")
        String address,

        @NotNull(message = "Username is required")
        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password

) {
}