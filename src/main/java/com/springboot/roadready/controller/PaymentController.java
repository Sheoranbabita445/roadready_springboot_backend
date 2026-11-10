package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.PaymentRequestDto;
import com.springboot.roadready.dto.response.PaymentFilterDto;
import com.springboot.roadready.dto.response.PaymentResponseDto;
import com.springboot.roadready.enums.PaymentMethod;
import com.springboot.roadready.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // create a payment for a reservation
    @PostMapping("/api/payment/add")
    public PaymentResponseDto createPayment(
            @RequestBody PaymentRequestDto dto,
            Principal principal) {
        return paymentService.createPayment(dto, principal.getName());
    }


    // get the payments made by the logged-in customer
    @GetMapping("/api/payment/my")
    public PaymentFilterDto getMyPayments(
            Principal principal,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return paymentService.getMyPayments(
                principal.getName(), page, size);
    }



    // get the COMPLETED payments for the admin
    @GetMapping("/api/admin/payment/all")
    public PaymentFilterDto getAllPayments(
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        return paymentService.getCompletedPayments(
                paymentMethod, fromDate, toDate, search, page, size);
    }

}
