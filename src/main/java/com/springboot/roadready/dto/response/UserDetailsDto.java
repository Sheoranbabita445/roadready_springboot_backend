package com.springboot.roadready.dto.response;

import com.springboot.roadready.enums.Role;

public record UserDetailsDto(
        String Username,
        Boolean isActive,
        Role role
) {
}
