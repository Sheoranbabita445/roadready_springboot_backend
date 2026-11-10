package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.response.ReservationResponseDto;
import com.springboot.roadready.model.Customer;
import com.springboot.roadready.model.Payment;
import com.springboot.roadready.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    // convert Reservation entity to DTO
    public static ReservationResponseDto convertEntityToDto(Reservation reservation) {
        return convertEntityToDto(reservation, null, false);
    }

    // convert Reservation entity to DTO together with its payment
    public static ReservationResponseDto convertEntityToDto(
            Reservation reservation, Payment payment, boolean reviewed) {

        Customer customer = reservation.getCustomer();

        return new ReservationResponseDto(
                reservation.getId(),
                customer.getUser().getUsername(),
                customer.getFirstName() + " " + customer.getLastName(),
                reservation.getCar().getId(),
                reservation.getCar().getModel(),
                reservation.getPickupAt(),
                reservation.getDropoffAt(),
                reservation.getPricePerDayAtBooking(),
                reservation.getPromoCode(),
                reservation.getDiscountAmount(),
                reservation.getTotalAmount(),
                reservation.getStatus() != null ? reservation.getStatus().name() : null,
                reservation.getCreatedAt(),
                reservation.getDamageCharge(),
                reservation.getDamageNotes(),
                payment != null ? payment.getPaymentStatus().name() : null,
                payment != null ? payment.getRefundAmount() : null,
                payment != null ? payment.getRefundedAt() : null,
                reservation.getCancellationRequestedAt(),
                reviewed
        );
    }
}
