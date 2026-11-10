package com.springboot.roadready.dto.response;

import java.util.Map;

// numbers for the admin home page (cards and donut charts), all counted in the database
public record AdminDashboardSummaryDto(
        Long totalCars,
        Long totalReservations,
        Long pendingReservations,
        Long cancellationRequests,
        Long totalCustomers,
        Long totalRentalAgents,
        Map<String, Long> reservationsByStatus,
        Map<String, Long> carsByStatus
) {
}
