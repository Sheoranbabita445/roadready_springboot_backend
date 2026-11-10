package com.springboot.roadready.dto.response;

import com.springboot.roadready.enums.Role;

//returns user details to admin
public record AdminUserResponseDto(
        Long id,
        String username,
        Role role,
        boolean active
) {
}