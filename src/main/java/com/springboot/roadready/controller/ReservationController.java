package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.DamageChargeRequestDto;
import com.springboot.roadready.dto.request.ModifyReservationRequestDto;
import com.springboot.roadready.dto.request.ReservationRequestDto;
import com.springboot.roadready.dto.response.CarAvailabilityResponseDto;
import com.springboot.roadready.dto.response.ReservationFilterDto;
import com.springboot.roadready.dto.response.ReservationResponseDto;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReservationController {

    private final ReservationService reservationService;


   // create a reservation
    @PostMapping("/api/reservation/add")
    public ReservationResponseDto createReservation(
            @RequestBody ReservationRequestDto reservationRequestDto,
            Principal principal) {

        // Get the logged-in customer's username
        String customerUsername = principal.getName();

        // Create the reservation
        return reservationService.createReservation(
                reservationRequestDto, customerUsername);
    }


    // to get reservations made by the logged-in customer
    @GetMapping("/api/reservation/my")
    public ReservationFilterDto getMyReservations(
            Principal principal,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return reservationService.getMyReservations(
                principal.getName(), status, page, size);
    }

    // Step 1 of cancellation (customer): a paid reservation becomes CANCELLATION_REQUESTED and waits for an admin
    @PutMapping("/api/reservation/{reservationId}/cancel")
    public ReservationResponseDto cancelReservation(
            @PathVariable Long reservationId,
            Principal principal) {

        return reservationService.cancelReservation(
                reservationId, principal.getName());
    }

    // to update reservation status
    @PutMapping("/api/admin/reservation/{reservationId}/status")
    public ReservationResponseDto updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestParam ReservationStatus status) {

        return reservationService.updateReservationStatus(
                reservationId, status);
    }

    // to get all reservations (admin)
    @GetMapping("/api/reservation/admin/all")
    public ReservationFilterDto getAllReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return reservationService.getAllReservations(status, search, page, size);
    }

    // cancellation requests waiting for the admin
    @GetMapping("/api/admin/reservation/cancellation-requests")
    public ReservationFilterDto getCancellationRequests(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return reservationService.getCancellationRequests(page, size);
    }

    // Step 2 of cancellation (admin): cancel the reservation and record the refund
    @PutMapping("/api/admin/reservation/{reservationId}/cancellation/approve")
    public ReservationResponseDto approveCancellation(@PathVariable Long reservationId) {
        return reservationService.approveCancellation(reservationId);
    }

    // admin declines the cancellation request: the reservation is confirmed again
    @PutMapping("/api/admin/reservation/{reservationId}/cancellation/reject")
    public ReservationResponseDto rejectCancellation(@PathVariable Long reservationId) {
        return reservationService.rejectCancellation(reservationId);
    }

    // to check car availability for the requested period
    @GetMapping("/api/reservation/check-availability")
    public CarAvailabilityResponseDto checkCarAvailability(
            @RequestParam Long carId,
            @RequestParam LocalDateTime pickupAt,
            @RequestParam LocalDateTime dropoffAt) {

        return reservationService.checkCarAvailability(
                carId, pickupAt, dropoffAt);
    }

    // to modify an existing reservation
    @PutMapping("/api/reservation/{reservationId}/modify")
    public ReservationResponseDto modifyReservation(
            @PathVariable Long reservationId,
            @RequestBody ModifyReservationRequestDto dto,
            Principal principal) {

        return reservationService.modifyReservation(
                reservationId,
                dto,
                principal.getName());
    }




   // to check in a customer and record vehicle pickup
    @PutMapping("/api/reservation/{reservationId}/check-in")
    public ReservationResponseDto checkInReservation(
            @PathVariable Long reservationId,
            Principal principal) {

        return reservationService.checkInReservation(
                reservationId,
                principal.getName());
    }




   // to check out a customer and record vehicle return
    @PutMapping("/api/reservation/{reservationId}/check-out")
    public ReservationResponseDto checkOutReservation(
            @PathVariable Long reservationId,
            Principal principal) {

        return reservationService.checkOutReservation(
                reservationId,
                principal.getName());
    }



   // add a damage charge to a reservation
    @PutMapping("/api/reservation/{reservationId}/damage-charge")
    public ReservationResponseDto addDamageCharge(
            @PathVariable Long reservationId,
            @RequestBody DamageChargeRequestDto dto,
            Principal principal) {

        // Get the logged-in rental agent's username
        String rentalAgentUsername = principal.getName();

        // Add the damage charge to the reservation
        return reservationService.addDamageCharge(
                reservationId, dto, rentalAgentUsername);
    }


}