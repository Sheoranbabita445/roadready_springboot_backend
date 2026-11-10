package com.springboot.roadready.model;

import com.springboot.roadready.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "pickup_at", nullable = false)
    private LocalDateTime pickupAt;

    @Column(name = "dropoff_at", nullable = false)
    private LocalDateTime dropoffAt;

    @Column(name = "actual_pickup_at")
    private LocalDateTime actualPickupAt;

    @Column(name = "actual_return_at")
    private LocalDateTime actualReturnAt;

    @Column(name = "price_per_day_at_booking", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerDayAtBooking;

    @Column(name = "promo_code", length = 50)
    private String promoCode;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "damage_charge", precision = 10, scale = 2)
    private BigDecimal damageCharge;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "damage_notes", length = 500)
    private String damageNotes;

    // set when a customer asks to cancel a paid reservation (status CANCELLATION_REQUESTED)
    @Column(name = "cancellation_requested_at")
    private LocalDateTime cancellationRequestedAt;
}