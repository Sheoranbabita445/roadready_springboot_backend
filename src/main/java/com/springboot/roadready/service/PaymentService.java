package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.PaymentRequestDto;
import com.springboot.roadready.dto.response.PaymentFilterDto;
import com.springboot.roadready.dto.response.PaginationDto;
import com.springboot.roadready.dto.response.PaymentResponseDto;
import com.springboot.roadready.enums.PaymentMethod;
import com.springboot.roadready.enums.PaymentStatus;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.mapper.PaymentMapper;
import com.springboot.roadready.model.Payment;
import com.springboot.roadready.model.Reservation;
import com.springboot.roadready.repository.PaymentRepository;
import com.springboot.roadready.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    // create a payment for a reservation
    @Transactional
    public PaymentResponseDto createPayment(
            PaymentRequestDto dto,
            String customerUsername) {

        log.info("Creating payment for customer: {}", customerUsername);

        // Validate the payment request
        if (dto == null || dto.reservationId() == null) {
            throw new InvalidCallException("Reservation ID is required");
        }

        // Validate the customer username
        if (customerUsername == null || customerUsername.isBlank()) {
            throw new InvalidCallException("Customer username is required");
        }

        log.info("Processing payment for reservation ID: {}", dto.reservationId());

        // Find the reservation using its ID (locked, so a double click cannot pay twice)
        Reservation reservation = reservationRepository
                .findByIdForUpdate(dto.reservationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reservation not found"));

        // Check whether the reservation belongs to the customer
        if (reservation.getCustomer() == null
                || reservation.getCustomer().getUser() == null
                || !customerUsername.equals(
                reservation.getCustomer().getUser().getUsername())) {
            throw new InvalidCallException(
                    "You are not allowed to make payment for this reservation");
        }

        // Check whether the reservation is pending
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidCallException(
                    "Only pending reservations can be paid");
        }

        // Validate the payment amount
        if (dto.amount() == null
                || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCallException(
                    "Payment amount must be greater than zero");
        }

        // Check whether the amount matches the reservation total
        if (reservation.getTotalAmount() == null
                || dto.amount().compareTo(reservation.getTotalAmount()) != 0) {
            throw new InvalidCallException(
                    "Payment amount must match the reservation total");
        }

        // Validate the payment method
        if (dto.paymentMethod() == null) {
            throw new InvalidCallException("Payment method is required");
        }

        // Prepare the payment
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalAmount());
        payment.setPaymentMethod(dto.paymentMethod());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());

        // transaction id shown to the customer on My Payments
        payment.setTransactionReference(
                "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());

        // Save the payment
        Payment savedPayment = paymentRepository.save(payment);

        // Confirm the reservation
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        log.info("Payment created successfully for reservation ID: {}", dto.reservationId());

        // Return the payment response
        return PaymentMapper.convertEntityToDto(savedPayment);
    }


    // get the payments of the logged-in customer
    public PaymentFilterDto getMyPayments(
            String customerUsername,
            Integer page,
            Integer size) {

        log.info("Fetching payments for customer: {}", customerUsername);

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        Page<PaymentResponseDto> paymentPage =
                paymentRepository.getPaymentsByCustomerUsername(customerUsername, pageable);

        log.info("Successfully fetched {} payments for customer: {}",
                paymentPage.getContent().size(), customerUsername);

        return new PaymentFilterDto(
                paymentPage.getContent(),
                new PaginationDto(paymentPage.getTotalPages(), paymentPage.getTotalElements(), paymentPage.getContent().size())
        );
    }


    // get the completed payments for the admin
    public PaymentFilterDto getCompletedPayments(
            PaymentMethod paymentMethod,
            LocalDate fromDate,
            LocalDate toDate,
            String search,
            Integer page,
            Integer size) {

        log.info("Fetching completed payments with filters");

        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new InvalidCallException("To date cannot be before from date");
        }

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // search text becomes a lower-case LIKE pattern (null = no search)
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.trim().toLowerCase() + "%";

        LocalDateTime paidFrom = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime paidBefore = toDate == null ? null : toDate.plusDays(1).atStartOfDay();

        Page<PaymentResponseDto> paymentPage = paymentRepository.filterPaymentsByStatus(
                PaymentStatus.COMPLETED,
                paymentMethod,
                paidFrom,
                paidBefore,
                searchPattern,
                pageable);

        log.info("Successfully fetched {} completed payments",
                paymentPage.getContent().size());

        return new PaymentFilterDto(
                paymentPage.getContent(),
                new PaginationDto(paymentPage.getTotalPages(), paymentPage.getTotalElements(), paymentPage.getContent().size())
        );
    }

}