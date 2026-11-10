package com.springboot.roadready.dto.response;

import java.util.List;
import java.util.Map;

// numbers for the logged-in customer's home page
public record CustomerDashboardSummaryDto(
        Long totalReservations,
        Map<String, Long> reservationsByStatus,
        List<ReservationResponseDto> upcoming
) {
}
