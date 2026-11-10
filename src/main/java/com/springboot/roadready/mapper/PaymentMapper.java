package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.response.PaymentResponseDto;
import com.springboot.roadready.model.Payment;

public class PaymentMapper {

    // convert Payment entity to PaymentResponseDto
    public static PaymentResponseDto convertEntityToDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getReservation().getCar().getModel(),
                payment.getReservation().getCustomer().getUser().getUsername(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTransactionReference(),
                payment.getPaymentDate(),
                payment.getRefundAmount(),
                payment.getRefundedAt()
        );
    }
}
