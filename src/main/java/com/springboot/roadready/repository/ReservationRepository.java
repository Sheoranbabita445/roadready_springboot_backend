package com.springboot.roadready.repository;

import com.springboot.roadready.enums.ReservationStatus;

import com.springboot.roadready.model.Reservation;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Reservations of one customer
    @Query("""
            SELECT r
            FROM Reservation r
            JOIN r.customer c
            JOIN c.user u
            WHERE u.username = ?1
            AND (?2 IS NULL OR r.status = ?2)
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    Page<Reservation> getReservationsByCustomerUsername(
            String customerUsername,
            ReservationStatus status,
            Pageable pageable
    );



    @Query("""
            SELECT r
            FROM Reservation r
            JOIN r.customer c
            JOIN c.user u
            JOIN r.car car
            WHERE (?1 IS NULL OR r.status = ?1)
            AND (?2 IS NULL OR LOWER(u.username) LIKE ?2
                 OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE ?2
                 OR LOWER(car.model) LIKE ?2)
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    Page<Reservation> filterReservations(
            ReservationStatus status,
            String searchPattern,
            Pageable pageable
    );


    // two clicks cannot both succeed
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.id = ?1
            """)
    Optional<Reservation> findByIdForUpdate(Long reservationId);


    // shows and assign in (confirmed or in progress), soonest pickup first
    @Query("""
            SELECT r
            FROM Reservation r
            JOIN r.customer c
            JOIN c.user u
            WHERE u.username = ?1
            AND r.status IN ?2
            ORDER BY r.pickupAt ASC
            """)
    List<Reservation> getUpcomingReservations(
            String customerUsername,
            List<ReservationStatus> statuses,
            Pageable pageable
    );


    // Number of reservations per status: all (admin), one customer, or the cars of one rental agent
    @Query("""
            SELECT r.status, COUNT(r)
            FROM Reservation r
            GROUP BY r.status
            """)
    List<Object[]> countReservationsByStatus();

    @Query("""
            SELECT r.status, COUNT(r)
            FROM Reservation r
            JOIN r.customer c
            JOIN c.user u
            WHERE u.username = ?1
            GROUP BY r.status
            """)
    List<Object[]> countReservationsByStatusForCustomer(String customerUsername);

    @Query("""
            SELECT r.status, COUNT(r)
            FROM Reservation r
            JOIN r.car car
            JOIN car.rentalAgent ra
            JOIN ra.user u
            WHERE u.username = ?1
            GROUP BY r.status
            """)
    List<Object[]> countReservationsByStatusForAgent(String agentUsername);


    // Method to count overlapping reservations for a car (availability check)
    @Query("""
              SELECT COUNT(r)
              FROM Reservation r
              WHERE r.car.id = ?1
              AND r.status IN ?4
              AND r.pickupAt < ?3
              AND r.dropoffAt > ?2
        """)
    Long countOverlappingReservations(
              Long carId,
              LocalDateTime pickupAt,
              LocalDateTime dropoffAt,
              List<ReservationStatus> statuses
    );


    //used for modifying reservation
    // count overlapping reservations, excluding the current reservation
    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.car.id = ?1
        AND r.id != ?2
        AND r.status IN ?5
        AND r.pickupAt < ?4
        AND r.dropoffAt > ?3
        """)
    Long countOverlappingReservationsExcludingCurrent(
            Long carId,
            Long reservationId,
            LocalDateTime pickupAt,
            LocalDateTime dropoffAt,
            List<ReservationStatus> statuses
    );


    // count all reservations made by a customer
    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.customer.id = ?1
        """)
    Long countReservationsByCustomerId(Long customerId);

    //  status is used here because only completed status reservation will get discount if more than 3
    // count completed reservations made by a customer
    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.customer.id = ?1
        AND r.status = ?2
        """)
    Long countReservationsByCustomerIdAndStatus(
            Long customerId,
            ReservationStatus status);

}
