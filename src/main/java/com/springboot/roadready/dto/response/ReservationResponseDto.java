package com.springboot.roadready.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponseDto(
        Long id,
        String customerUsername,
        String customerName,
        Long carId,
        String carModel,
        LocalDateTime pickupAt,
        LocalDateTime dropoffAt,
        BigDecimal pricePerDayAtBooking,
        String promoCode,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        BigDecimal damageCharge,
        String damageNotes,
        // status of the payment made for this reservation (COMPLETED, REFUNDED) or null if nothing was paid
        String paymentStatus,
        BigDecimal refundAmount,
        LocalDateTime refundedAt,
        LocalDateTime cancellationRequestedAt,
        // true once the customer has reviewed this reservation
        Boolean reviewed
) {
}
