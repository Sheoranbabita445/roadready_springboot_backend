package com.springboot.roadready.dto.response;

import java.util.Map;

// numbers for the logged-in rental agent only: cars assigned to them and reservations on those cars
public record RentalAgentDashboardSummaryDto(
        Long assignedCars,
        Long totalReservations,
        Map<String, Long> carsByStatus,
        Map<String, Long> reservationsByStatus
) {
}
