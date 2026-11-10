
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.DamageChargeRequestDto;
import com.springboot.roadready.dto.request.ModifyReservationRequestDto;
import com.springboot.roadready.dto.request.ReservationRequestDto;
import com.springboot.roadready.dto.response.CarAvailabilityResponseDto;
import com.springboot.roadready.dto.response.CustomerDashboardSummaryDto;
import com.springboot.roadready.dto.response.PaginationDto;
import com.springboot.roadready.dto.response.ReservationFilterDto;
import com.springboot.roadready.dto.response.ReservationResponseDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.enums.PaymentStatus;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.OwnershipException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.mapper.ReservationMapper;
import com.springboot.roadready.model.Car;
import com.springboot.roadready.model.Customer;
import com.springboot.roadready.model.Payment;
import com.springboot.roadready.model.RentalAgent;
import com.springboot.roadready.model.Reservation;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.CustomerRepository;
import com.springboot.roadready.repository.PaymentRepository;
import com.springboot.roadready.repository.ReservationRepository;
import com.springboot.roadready.repository.ReviewRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalAgentRepository rentalAgentRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    // Reservations in these statuses keep the car's dates blocked.
    // A reservation waiting for its cancellation to be approved still holds the car,
    // because the admin may reject the request.
    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.ACTIVE,
            ReservationStatus.CANCELLATION_REQUESTED
    );

    public ReservationService(
            ReservationRepository reservationRepository,
            CarRepository carRepository,
            CustomerRepository customerRepository,
            RentalAgentRepository rentalAgentRepository,
            PaymentRepository paymentRepository,
            ReviewRepository reviewRepository) {
        this.reservationRepository = reservationRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.rentalAgentRepository = rentalAgentRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    // Method to create a reservation
    @Transactional
    public ReservationResponseDto createReservation(
            ReservationRequestDto reservationRequestDto,
            String customerUsername) {

        log.info("Creating reservation for customer: {}", customerUsername);

        // Find the logged-in customer
        Customer customer = customerRepository.getByUsername(customerUsername);

        if (customer == null) {
            throw new ResourceNotFoundException("Customer profile not found");
        }

        // Validate the pickup and drop-off dates
        LocalDateTime pickupAt = reservationRequestDto.pickupAt();
        LocalDateTime dropoffAt = reservationRequestDto.dropoffAt();

        if (pickupAt == null || dropoffAt == null) {
            throw new InvalidCallException("Pickup and drop-off dates are required");
        }

        if (!pickupAt.isAfter(LocalDateTime.now())) {
            throw new InvalidCallException("Pickup date must be in the future");
        }

        if (!dropoffAt.isAfter(pickupAt)) {
            throw new InvalidCallException("Drop-off must be after pickup");
        }

        // Find the selected car
        Car car = carRepository.findById(reservationRequestDto.carId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Car not found"));

        // Check whether the car is available for rental
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCallException(
                    "Selected car is not available for rental");
        }

        // Calculate the rental duration in days
        long days = ChronoUnit.DAYS.between(
                pickupAt.toLocalDate(),
                dropoffAt.toLocalDate());

        // Minimum rental duration should be at least one day
        if (days < 1) {
            days = 1;
        }

        // Calculate the base rental amount
        BigDecimal baseAmount = car.getPricePerDay()
                .multiply(BigDecimal.valueOf(days));

        // Get the customer's total reservation count
        Long totalReservations =
                reservationRepository.countReservationsByCustomerId(
                        customer.getId());

        // Initialize the discount amount and promo code
        BigDecimal discountAmount = BigDecimal.ZERO;
        String promoCode = null;

        // Apply a 10% discount if this is the customer's first reservation
        if (totalReservations == 0) {
            discountAmount = baseAmount
                    .multiply(new BigDecimal("0.10"))
                    .setScale(2, RoundingMode.HALF_UP);

            promoCode = "WELCOME10";
        } else {
            // Get the customer's completed reservation count
            Long completedReservations =
                    reservationRepository
                            .countReservationsByCustomerIdAndStatus(
                                    customer.getId(),
                                    ReservationStatus.COMPLETED);

            // Apply a 10% loyalty discount after three completed reservations
            if (completedReservations >= 3) {
                discountAmount = baseAmount
                        .multiply(new BigDecimal("0.10"))
                        .setScale(2, RoundingMode.HALF_UP);

                promoCode = "LOYALTY10";
            }
        }

        // Initialize the damage charge to zero
        BigDecimal damageCharge = BigDecimal.ZERO;

        // Calculate the total amount after applying the discount
        BigDecimal totalAmount = baseAmount
                .subtract(discountAmount)
                .add(damageCharge);

        // Prepare the reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setCar(car);
        reservation.setPickupAt(pickupAt);
        reservation.setDropoffAt(dropoffAt);
        reservation.setPricePerDayAtBooking(car.getPricePerDay());
        reservation.setPromoCode(promoCode);
        reservation.setDiscountAmount(discountAmount);
        reservation.setDamageCharge(damageCharge);
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());

        // Save the reservation
        Reservation savedReservation =
                reservationRepository.save(reservation);

        log.info("Reservation created successfully with ID: {} for customer: {}",
                savedReservation.getId(), customerUsername);

        // Convert the saved reservation to a response DTO
        return toDto(savedReservation);
    }


    // get the reservations of the logged-in customer
    public ReservationFilterDto getMyReservations(
            String customerUsername, ReservationStatus status, Integer page, Integer size) {

        log.info("Fetching reservations for customer: {}", customerUsername);

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch one page of reservations belonging to the customer
        Page<Reservation> reservationPage =
                reservationRepository.getReservationsByCustomerUsername(
                        customerUsername, status, pageable);

        log.info("Successfully fetched {} reservations for customer: {}",
                reservationPage.getContent().size(), customerUsername);

        return toFilterDto(reservationPage);
    }


    // Step 1 of the cancellation the customer asks to cancel
    // a paid reservation (a COMPLETED payment exists) becomes CANCELLATION_REQUESTED and waits for an admin
    // Nothing is refunded and the reservation is NOT cancelled yet.
    // a reservation that was never paid has nothing to refund, so it is cancelled straight away.
    @Transactional
    public ReservationResponseDto cancelReservation(Long reservationId, String customerUsername) {

        log.info("Processing cancellation request for reservation ID: {} by customer: {}",
                reservationId, customerUsername);

        // Find the reservation (locked, so a double click cannot create two requests)
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Check whether the reservation belongs to the logged-in customer
        if (!reservation.getCustomer().getUser().getUsername().equals(customerUsername)) {
            throw new OwnershipException("You can cancel only your own reservations");
        }

        // Duplicate or late requests get a clear message
        if (reservation.getStatus() == ReservationStatus.CANCELLATION_REQUESTED) {
            throw new InvalidCallException(
                    "A cancellation request was already submitted and is waiting for admin approval");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidCallException("This reservation is already cancelled");
        }

        // Allow cancellation only for pending or confirmed reservations
        if (reservation.getStatus() != ReservationStatus.PENDING &&
                reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidCallException("Only pending or confirmed reservations can be cancelled");
        }

        boolean paid = paymentRepository.countByReservationIdAndStatus(
                reservationId, PaymentStatus.COMPLETED) > 0;

        if (paid) {
            reservation.setStatus(ReservationStatus.CANCELLATION_REQUESTED);
            reservation.setCancellationRequestedAt(LocalDateTime.now());
        } else {
            reservation.setStatus(ReservationStatus.CANCELLED);
        }

        // Save the reservation
        Reservation saved = reservationRepository.save(reservation);

        log.info("Cancellation processed successfully for reservation ID: {}. New status: {}",
                reservationId, saved.getStatus());

        return toDto(saved);
    }


    // Step 2 of the cancellation (admin) approve the request
    // The reservation becomes CANCELLED and its completed payment is recorded as REFUNDED.
    @Transactional
    public ReservationResponseDto approveCancellation(Long reservationId) {

        log.info("Approving cancellation for reservation ID: {}", reservationId);

        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidCallException("This cancellation was already approved");
        }
        if (reservation.getStatus() != ReservationStatus.CANCELLATION_REQUESTED) {
            throw new InvalidCallException(
                    "Only reservations with a pending cancellation request can be approved");
        }

        // Record the refund on the completed payment
        List<Payment> payments = paymentRepository.getPaymentsByReservationId(reservationId);
        for (Payment payment : payments) {
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setRefundAmount(payment.getAmount());
                payment.setRefundedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        log.info("Cancellation approved successfully for reservation ID: {}", reservationId);

        return toDto(saved);
    }


    // Admin declines the request: the reservation goes back to CONFIRMED and the payment is left as it is
    @Transactional
    public ReservationResponseDto rejectCancellation(Long reservationId) {

        log.info("Rejecting cancellation request for reservation ID: {}", reservationId);

        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CANCELLATION_REQUESTED) {
            throw new InvalidCallException(
                    "Only reservations with a pending cancellation request can be rejected");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCancellationRequestedAt(null);
        Reservation saved = reservationRepository.save(reservation);

        log.info("Cancellation rejected successfully for reservation ID: {}", reservationId);

        return toDto(saved);
    }


    // update reservation status by admin
    public ReservationResponseDto updateReservationStatus(
            Long reservationId,
            ReservationStatus status) {

        log.info("Updating reservation status for reservation ID: {}", reservationId);

        // Find the reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Validate the requested status
        if (status == null) {
            throw new InvalidCallException("Reservation status is required");
        }

        // Allow admin only to confirm pending reservations
        if (reservation.getStatus() == ReservationStatus.PENDING
                && status == ReservationStatus.CONFIRMED) {

            reservation.setStatus(status);

        } else {
            throw new InvalidCallException(
                    "Admin can only confirm pending reservations");
        }

        // Save the updated reservation
        Reservation updatedReservation = reservationRepository.save(reservation);

        log.info("Reservation status updated successfully. Reservation ID: {}, Status: {}",
                reservationId, updatedReservation.getStatus());

        // Return the updated reservation details
        return toDto(updatedReservation);
    }


    // get all reservations (admin)
    public ReservationFilterDto getAllReservations(
            ReservationStatus status, String search, Integer page, Integer size) {

        log.info("Fetching all reservations for admin");

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);
        // search text becomes a lower-case LIKE pattern (null = no search)
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.trim().toLowerCase() + "%";

        Page<Reservation> reservationPage = reservationRepository.filterReservations(
                status, searchPattern, pageable);

        log.info("Successfully fetched {} reservations for admin",
                reservationPage.getContent().size());

        return toFilterDto(reservationPage);
    }


    // get the cancellation requests waiting for the admin
    public ReservationFilterDto getCancellationRequests(Integer page, Integer size) {

        log.info("Fetching cancellation requests waiting for admin approval");

        return getAllReservations(ReservationStatus.CANCELLATION_REQUESTED, null, page, size);
    }


    // customer's home page
    public CustomerDashboardSummaryDto getCustomerDashboardSummary(
            String customerUsername) {

        log.info("Fetching dashboard summary for customer: {}", customerUsername);

        Map<String, Long> byStatus = emptyStatusMap();
        long total = 0;
        for (Object[] row : reservationRepository.countReservationsByStatusForCustomer(customerUsername)) {
            long count = ((Number) row[1]).longValue();
            byStatus.put(((ReservationStatus) row[0]).name(), count);
            total += count;
        }

        // next three trips: confirmed or in progress, soonest pickup first
        List<Reservation> upcoming = reservationRepository.getUpcomingReservations(
                customerUsername,
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.ACTIVE),
                PageRequest.of(0, 3));

        log.info("Customer dashboard summary fetched successfully for: {}. Total reservations: {}",
                customerUsername, total);

        return new CustomerDashboardSummaryDto(
                total,
                byStatus,
                upcoming.stream().map(this::toDto).toList());
    }


    // Reservation counts for the cars of one rental agent (agent dashboard donut chart)
    public Map<String, Long> countReservationsByStatusForAgent(String agentUsername) {

        log.info("Fetching reservation counts for rental agent: {}", agentUsername);

        Map<String, Long> byStatus = emptyStatusMap();
        for (Object[] row : reservationRepository.countReservationsByStatusForAgent(agentUsername)) {
            byStatus.put(((ReservationStatus) row[0]).name(), ((Number) row[1]).longValue());
        }

        log.info("Reservation counts fetched successfully for rental agent: {}", agentUsername);

        return byStatus;
    }


    // Reservation counts for the whole system (admin dashboard donut chart)
    public Map<String, Long> countReservationsByStatus() {

        log.info("Fetching reservation counts for admin dashboard");

        Map<String, Long> byStatus = emptyStatusMap();
        for (Object[] row : reservationRepository.countReservationsByStatus()) {
            byStatus.put(((ReservationStatus) row[0]).name(), ((Number) row[1]).longValue());
        }

        log.info("Reservation counts fetched successfully for admin dashboard");

        return byStatus;
    }


    // Every status is present (with 0) so the charts and cards always have all keys
    private Map<String, Long> emptyStatusMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (ReservationStatus status : ReservationStatus.values()) {
            map.put(status.name(), 0L);
        }
        return map;
    }


    // Converts a page of reservations, looking up the payments and reviews of the whole page in two queries
    private ReservationFilterDto toFilterDto(Page<Reservation> reservationPage) {

        List<Reservation> reservations = reservationPage.getContent();
        Map<Long, Payment> paymentByReservation = new HashMap<>();
        Set<Long> reviewedIds = new HashSet<>();

        if (!reservations.isEmpty()) {
            List<Long> ids = reservations.stream().map(Reservation::getId).toList();

            // payments come ordered by id, so a later payment replaces an earlier one
            for (Payment payment : paymentRepository.getPaymentsByReservationIds(ids)) {
                paymentByReservation.put(payment.getReservation().getId(), payment);
            }
            reviewedIds.addAll(reviewRepository.getReviewedReservationIds(ids));
        }

        List<ReservationResponseDto> list = reservations.stream()
                .map(r -> ReservationMapper.convertEntityToDto(
                        r, paymentByReservation.get(r.getId()), reviewedIds.contains(r.getId())))
                .toList();

        return new ReservationFilterDto(
                list,
                new PaginationDto(reservationPage.getTotalPages(), reservationPage.getTotalElements(), reservationPage.getContent().size())
        );
    }


    // Converts one reservation, looking up its payment and review
    private ReservationResponseDto toDto(Reservation reservation) {
        List<Payment> payments = paymentRepository.getPaymentsByReservationId(reservation.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(0);
        boolean reviewed = reviewRepository.countReviewsByReservationId(reservation.getId()) > 0;
        return ReservationMapper.convertEntityToDto(reservation, payment, reviewed);
    }


    // check whether a car is available for the requested period
    public CarAvailabilityResponseDto checkCarAvailability(
            Long carId,
            LocalDateTime pickupAt,
            LocalDateTime dropoffAt) {

        log.info("Checking availability for car ID: {}", carId);

        // Validate the car ID and requested dates
        if (carId == null || pickupAt == null || dropoffAt == null
                || !dropoffAt.isAfter(pickupAt)) {
            throw new InvalidCallException(
                    "Invalid car ID, pickup or drop-off date");
        }

        // Check whether the pickup date is in the future
        if (!pickupAt.isAfter(LocalDateTime.now())) {
            throw new InvalidCallException(
                    "Pickup date must be in the future");
        }

        // Find the car using its ID
        Car car = carRepository.findById(carId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Car not found"));

        // Check whether the car is available for rental
        if (car.getStatus() != CarStatus.AVAILABLE) {

            log.info("Car ID: {} is unavailable because its status is: {}",
                    carId, car.getStatus());

            return new CarAvailabilityResponseDto(
                    carId,
                    pickupAt,
                    dropoffAt,
                    "UNAVAILABLE",
                    "Car is not available for rental");
        }

        // Count reservations overlapping with the requested period
        Long count = reservationRepository.countOverlappingReservations(
                carId, pickupAt, dropoffAt, BLOCKING_STATUSES);

        // Return the availability result
        if (count == 0) {

            log.info("Car ID: {} is available for the requested period", carId);

            return new CarAvailabilityResponseDto(
                    carId,
                    pickupAt,
                    dropoffAt,
                    "AVAILABLE",
                    "Car is available for the requested period");
        }

        log.info("Car ID: {} is unavailable due to overlapping reservations", carId);

        return new CarAvailabilityResponseDto(
                carId,
                pickupAt,
                dropoffAt,
                "UNAVAILABLE",
                "Car is already reserved for the requested period");
    }


    // to modify an existing reservation
    public ReservationResponseDto modifyReservation(
            Long reservationId,
            ModifyReservationRequestDto dto,
            String customerUsername) {

        log.info("Modifying reservation ID: {} for customer: {}",
                reservationId, customerUsername);

        // Find the reservation using its ID
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Check whether the reservation belongs to the customer
        if (!reservation.getCustomer().getUser().getUsername().equals(customerUsername)) {
            throw new OwnershipException("You are not allowed to modify this reservation");
        }

        // Check whether the reservation can be modified
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidCallException("Only pending or confirmed reservations can be modified");
        }

        // Validate the new pickup and drop-off dates
        if (dto.pickupAt() == null || dto.dropoffAt() == null
                || !dto.dropoffAt().isAfter(dto.pickupAt())) {
            throw new InvalidCallException("Invalid pickup or drop-off date");
        }

        // Check whether the car is available for the new period
        Long count = reservationRepository.countOverlappingReservationsExcludingCurrent(
                reservation.getCar().getId(),
                reservationId,
                dto.pickupAt(),
                dto.dropoffAt(),
                BLOCKING_STATUSES
        );

        // Stop modification if another reservation overlaps
        if (count > 0) {
            throw new InvalidCallException("Car is not available for the requested period");
        }

        // Update the reservation dates
        reservation.setPickupAt(dto.pickupAt());
        reservation.setDropoffAt(dto.dropoffAt());

        // Calculate the new total amount using the original booking price
        long days = ChronoUnit.DAYS.between(dto.pickupAt(), dto.dropoffAt());

        if (dto.dropoffAt().toLocalTime().isAfter(dto.pickupAt().toLocalTime())) {
            days++;
        }

        BigDecimal totalAmount = reservation.getPricePerDayAtBooking()
                .multiply(BigDecimal.valueOf(days));

        // Update the reservation total
        reservation.setTotalAmount(totalAmount);

        // Save the modified reservation
        Reservation updatedReservation = reservationRepository.save(reservation);

        log.info("Reservation modified successfully. Reservation ID: {}", reservationId);

        // Convert the updated reservation to DTO
        return toDto(updatedReservation);
    }


    // to check in a customer and record actual vehicle pickup
    @Transactional
    public ReservationResponseDto checkInReservation(
            Long reservationId,
            String rentalAgentUsername) {

        log.info("Checking in reservation ID: {} by rental agent: {}",
                reservationId, rentalAgentUsername);

        // Find the reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Find the rental agent by username
        RentalAgent rentalAgent = rentalAgentRepository
                .getByUsername(rentalAgentUsername);

        if (rentalAgent == null) {
            throw new ResourceNotFoundException("Rental agent not found");
        }

        // Check whether the reservation is confirmed
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidCallException(
                    "Only confirmed reservations can be checked in");
        }

        // Record the actual pickup time
        reservation.setActualPickupAt(LocalDateTime.now());

        // Update the reservation status
        reservation.setStatus(ReservationStatus.ACTIVE);

        // Save the updated reservation
        Reservation updatedReservation = reservationRepository.save(reservation);

        log.info("Reservation checked in successfully. Reservation ID: {}", reservationId);

        // Return the updated reservation details
        return toDto(updatedReservation);
    }


    // to check out a customer and record the actual vehicle return
    @Transactional
    public ReservationResponseDto checkOutReservation(
            Long reservationId,
            String rentalAgentUsername) {

        log.info("Checking out reservation ID: {} by rental agent: {}",
                reservationId, rentalAgentUsername);

        // Find the reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Find the rental agent by username
        RentalAgent rentalAgent = rentalAgentRepository
                .getByUsername(rentalAgentUsername);

        if (rentalAgent == null) {
            throw new ResourceNotFoundException("Rental agent not found");
        }

        // Check whether the reservation is active
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new InvalidCallException(
                    "Only active reservations can be checked out");
        }

        // Check whether the rental agent is assigned to the car
        if (!reservation.getCar().getRentalAgent().getId().equals(rentalAgent.getId())) {
            throw new InvalidCallException(
                    "This rental agent is not assigned to the car");
        }

        // Record the actual return time
        reservation.setActualReturnAt(LocalDateTime.now());

        // Update the reservation status
        reservation.setStatus(ReservationStatus.COMPLETED);

        // Save the updated reservation
        Reservation updatedReservation = reservationRepository.save(reservation);

        log.info("Reservation checked out successfully. Reservation ID: {}", reservationId);

        // Return the updated reservation details
        return toDto(updatedReservation);
    }


    // add a damage charge to a reservation
    @Transactional
    public ReservationResponseDto addDamageCharge(
            Long reservationId,
            DamageChargeRequestDto dto,
            String rentalAgentUsername) {

        log.info("Adding damage charge to reservation ID: {} by rental agent: {}",
                reservationId, rentalAgentUsername);

        // Validate the damage charge request
        if (dto == null || dto.damageCharge() == null) {
            throw new InvalidCallException("Damage charge is required");
        }

        // Check whether the damage charge is negative
        if (dto.damageCharge().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidCallException("Damage charge cannot be negative");
        }

        // Find the reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reservation not found"));

        // Find the rental agent
        RentalAgent rentalAgent = rentalAgentRepository.getByUsername(
                rentalAgentUsername);

        if (rentalAgent == null) {
            throw new ResourceNotFoundException("Rental agent not found");
        }

        // Check whether the rental agent is assigned to this car
        if (!reservation.getCar().getRentalAgent().getId()
                .equals(rentalAgent.getId())) {
            throw new InvalidCallException(
                    "You are not authorized to add damage charges for this car");
        }

        // Check whether the reservation is active or completed
        if (reservation.getStatus() != ReservationStatus.ACTIVE
                && reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new InvalidCallException(
                    "Damage charges can only be added to active or completed reservations");
        }

        // Update the damage charge and notes
        reservation.setDamageCharge(dto.damageCharge());
        reservation.setDamageNotes(dto.damageNotes());

        // Calculate the rental duration in days
        long days = ChronoUnit.DAYS.between(
                reservation.getPickupAt().toLocalDate(),
                reservation.getDropoffAt().toLocalDate());

        // Ensure the rental duration is at least one day
        if (days < 1) {
            days = 1;
        }

        // Calculate the base rental amount
        BigDecimal baseAmount = reservation.getPricePerDayAtBooking()
                .multiply(BigDecimal.valueOf(days));

        // Get the existing discount amount
        BigDecimal discountAmount = reservation.getDiscountAmount() == null
                ? BigDecimal.ZERO
                : reservation.getDiscountAmount();

        // Calculate the total amount after discount and damage charge
        BigDecimal totalAmount = baseAmount
                .subtract(discountAmount)
                .add(dto.damageCharge());

        // Update the reservation total
        reservation.setTotalAmount(totalAmount);

        // Save the updated reservation
        Reservation updatedReservation = reservationRepository.save(reservation);

        log.info("Damage charge added successfully to reservation ID: {}", reservationId);

        // Convert the updated reservation to a response DTO
        return toDto(updatedReservation);
    }
}
