package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.CustomerRequestDto;
import com.springboot.roadready.dto.response.CustomerDashboardSummaryDto;
import com.springboot.roadready.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/api/customer/add")
    public void addCustomer(
            @Valid @RequestBody CustomerRequestDto customerRequestDto) {
        customerService.addCustomer(customerRequestDto);
    }

    // logged-in customer's home page
    @GetMapping("/api/customer/dashboard/summary")
    public CustomerDashboardSummaryDto getDashboardSummary(Principal principal) {
        return customerService.getDashboardSummary(principal.getName());
    }
}
