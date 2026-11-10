package com.springboot.roadready.dto.response;

import java.time.Instant;

public record ErrorDto(
        String message,
        String comments,
        Instant timestamp
) {
}