package com.springboot.roadready.dto.response;

public record LocationResponseDto(
        Long id,
        String city,
        String state,
        String address
) {
}
