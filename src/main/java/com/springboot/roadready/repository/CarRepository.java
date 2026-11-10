package com.springboot.roadready.repository;


import com.springboot.roadready.dto.response.CarResponseDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.model.Car;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {



    @Query("""
    SELECT new com.springboot.roadready.dto.response.CarResponseDto(
        c.id,
        b.id,
        b.name,
        l.id,
        l.city,
        ra.id,
        u.username,
        c.registrationNumber,
        c.model,
        c.manufactureYear,
        c.vehicleType,
        c.fuelType,
        c.transmission,
        c.seats,
        c.pricePerDay,
        c.status,
        c.imageUrl,
        c.maintenanceNote
    )
    FROM Car c
    JOIN c.brand b
    JOIN c.location l
    JOIN c.rentalAgent ra
    JOIN ra.user u
    WHERE (?1 IS NULL OR b.id = ?1)
    AND (?2 IS NULL OR l.id = ?2)
    AND (?3 IS NULL OR c.fuelType = ?3)
    AND (?4 IS NULL OR c.transmission = ?4)
    AND (?5 IS NULL OR c.vehicleType = ?5)
    AND (?6 IS NULL OR c.pricePerDay >= ?6)
    AND (?7 IS NULL OR c.pricePerDay <= ?7)
    AND (?8 IS NULL OR c.status = ?8)
    AND (?9 IS NULL OR LOWER(c.model) LIKE ?9
         OR LOWER(c.registrationNumber) LIKE ?9
         OR LOWER(b.name) LIKE ?9)
    ORDER BY c.id DESC
    """)
    Page<CarResponseDto> dynamicFilterCar(
            Long brandId,
            Long locationId,
            String fuelType,
            String transmission,
            String vehicleType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CarStatus status,
            String searchPattern,
            Pageable pageable
    );


    // Cars assigned rental agent
    @Query("""
    SELECT new com.springboot.roadready.dto.response.CarResponseDto(
        c.id,
        b.id,
        b.name,
        l.id,
        l.city,
        ra.id,
        u.username,
        c.registrationNumber,
        c.model,
        c.manufactureYear,
        c.vehicleType,
        c.fuelType,
        c.transmission,
        c.seats,
        c.pricePerDay,
        c.status,
        c.imageUrl,
        c.maintenanceNote
    )
    FROM Car c
    JOIN c.brand b
    JOIN c.location l
    JOIN c.rentalAgent ra
    JOIN ra.user u
    WHERE u.username = ?1
    AND (?2 IS NULL OR c.status = ?2)
    AND (?3 IS NULL OR LOWER(c.model) LIKE ?3
         OR LOWER(c.registrationNumber) LIKE ?3
         OR LOWER(b.name) LIKE ?3)
    ORDER BY c.id DESC
    """)
    Page<CarResponseDto> getCarsByRentalAgentUsername(
            String username,
            CarStatus status,
            String searchPattern,
            Pageable pageable
    );


    // Number of cars per status (admin dashboard donut chart)
    @Query("""
        SELECT c.status, COUNT(c)
        FROM Car c
        GROUP BY c.status
        """)
    List<Object[]> countCarsByStatus();


    // Number of cars per status for one rental agent (agent dashboard donut chart)
    @Query("""
        SELECT c.status, COUNT(c)
        FROM Car c
        JOIN c.rentalAgent ra
        JOIN ra.user u
        WHERE u.username = ?1
        GROUP BY c.status
        """)
    List<Object[]> countCarsByStatusForAgent(String username);


    // update the status of a car assigned to a rental agent
    @Modifying
    @Transactional
    @Query("""
        UPDATE Car c
        SET c.status = ?3
        WHERE c.id = ?1
        AND c.rentalAgent.id = (
            SELECT ra.id
            FROM RentalAgent ra
            JOIN ra.user u
            WHERE u.username = ?2
        )
        """)
    int updateCarStatusByRentalAgent(
            Long carId,
            String username,
            CarStatus status
    );


    // update maintenance details for a car assigned to a rental agent
    @Modifying
    @Transactional
    @Query("""
        UPDATE Car c
        SET c.maintenanceNote = ?3
        WHERE c.id = ?1
        AND c.rentalAgent.id = (
            SELECT ra.id
            FROM RentalAgent ra
            JOIN ra.user u
            WHERE u.username = ?2
        )
        """)
    int updateMaintenanceNoteByRentalAgent(
            Long carId,
            String username,
            String maintenanceNote
    );

}
