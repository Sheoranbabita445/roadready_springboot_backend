package com.springboot.roadready.repository;

import com.springboot.roadready.dto.response.PaymentResponseDto;
import com.springboot.roadready.enums.PaymentMethod;
import com.springboot.roadready.enums.PaymentStatus;
import com.springboot.roadready.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Payments made by one customer, newest first
    @Query("""
        SELECT new com.springboot.roadready.dto.response.PaymentResponseDto(
            p.id, r.id, car.model, u.username, p.amount, p.paymentMethod,
            p.paymentStatus, p.transactionReference, p.paymentDate,
            p.refundAmount, p.refundedAt
        )
        FROM Payment p
        JOIN p.reservation r
        JOIN r.car car
        JOIN r.customer c
        JOIN c.user u
        WHERE u.username = ?1
        ORDER BY p.id DESC
        """)
    Page<PaymentResponseDto> getPaymentsByCustomerUsername(
            String username,
            Pageable pageable
    );




    @Query("""
        SELECT new com.springboot.roadready.dto.response.PaymentResponseDto(
            p.id, r.id, car.model, u.username, p.amount, p.paymentMethod,
            p.paymentStatus, p.transactionReference, p.paymentDate,
            p.refundAmount, p.refundedAt
        )
        FROM Payment p
        JOIN p.reservation r
        JOIN r.car car
        JOIN r.customer c
        JOIN c.user u
        WHERE p.paymentStatus = ?1
        AND (?2 IS NULL OR p.paymentMethod = ?2)
        AND (?3 IS NULL OR p.paymentDate >= ?3)
        AND (?4 IS NULL OR p.paymentDate < ?4)
        AND (?5 IS NULL OR LOWER(u.username) LIKE ?5
             OR LOWER(car.model) LIKE ?5
             OR LOWER(p.transactionReference) LIKE ?5)
        ORDER BY p.id DESC
        """)
    Page<PaymentResponseDto> filterPaymentsByStatus(
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            LocalDateTime paidFrom,
            LocalDateTime paidBefore,
            String searchPattern,
            Pageable pageable
    );



    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.reservation.id = ?1
        ORDER BY p.id DESC
        """)
    List<Payment> getPaymentsByReservationId(Long reservationId);


    // payments of the reservation have this status (eg COMPLETED = paid and not refunded yet)
    @Query("""
        SELECT COUNT(p)
        FROM Payment p
        WHERE p.reservation.id = ?1
        AND p.paymentStatus = ?2
        """)
    Long countByReservationIdAndStatus(Long reservationId, PaymentStatus paymentStatus);


    // Payments of many reservations at once
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.reservation.id IN ?1
        ORDER BY p.id
        """)
    List<Payment> getPaymentsByReservationIds(Collection<Long> reservationIds);

}
