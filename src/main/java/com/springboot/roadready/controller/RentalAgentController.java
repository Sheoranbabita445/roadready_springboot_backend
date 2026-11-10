package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.RentalAgentRequestDto;
import com.springboot.roadready.dto.response.RentalAgentDashboardSummaryDto;
import com.springboot.roadready.service.RentalAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RentalAgentController {
    private final RentalAgentService rentalAgentService;

    @PostMapping("/api/rentalagent/add")
    public void addRentalAgent(@Valid @RequestBody RentalAgentRequestDto rentalAgentRequestDto){
        rentalAgentService.addRentalAgent(rentalAgentRequestDto);
    }

    // logged-in rental agent's home page
    @GetMapping("/api/rentalagent/dashboard/summary")
    public RentalAgentDashboardSummaryDto getDashboardSummary(Principal principal) {
        return rentalAgentService.getDashboardSummary(principal.getName());
    }
}
