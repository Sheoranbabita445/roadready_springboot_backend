package com.springboot.roadready.dto.response;

import com.springboot.roadready.enums.PaymentMethod;
import com.springboot.roadready.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long reservationId,
        String carModel,
        String customerUsername,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String transactionReference,
        LocalDateTime paymentDate,
        BigDecimal refundAmount,
        LocalDateTime refundedAt
) {}
