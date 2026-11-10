package com.springboot.roadready.model;

import com.springboot.roadready.enums.CarStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private RentalAgent rentalAgent;

    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @Column(name = "model", length = 80, nullable = false)
    private String model;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType;

    @Column(name = "fuel_type", length = 20)
    private String fuelType;

    @Column(name = "transmission", length = 20)
    private String transmission;

    private Integer seats;

    @Column(name = "price_per_day", precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "maintenance_note", length = 500)
    private String maintenanceNote;
}