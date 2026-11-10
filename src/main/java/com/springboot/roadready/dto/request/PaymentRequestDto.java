package com.springboot.roadready.dto.request;

import com.springboot.roadready.enums.PaymentMethod;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// DTO to make a payment
public record PaymentRequestDto(

        @NotNull(message = "Reservation ID is required")
        Long reservationId,

        @NotNull(message = "Payment amount is required")
        @Positive(message = "Payment amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod

) {}