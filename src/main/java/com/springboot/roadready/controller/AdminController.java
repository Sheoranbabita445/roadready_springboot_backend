package com.springboot.roadready.controller;


import com.springboot.roadready.dto.request.AdminRequestDto;
import com.springboot.roadready.dto.response.AdminDashboardSummaryDto;
import com.springboot.roadready.dto.response.AdminUserFilterDto;
import com.springboot.roadready.dto.response.AdminUserResponseDto;
import com.springboot.roadready.dto.response.RentalAgentResponseDto;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/api/admin/add")
    public void addAdmin(@Valid @RequestBody AdminRequestDto adminRequestDto){
        adminService.addAdmin(adminRequestDto);
    }


    // get all users by admin
    @GetMapping("/api/admin/user/all")
    public AdminUserFilterDto getAllUser(
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {

        return adminService.getAllUsers(role, search, page, size);
    }

    // update user status by admin
    @PutMapping("/api/admin/user/{userId}/status")
    public AdminUserResponseDto updateUserStatus(
            @PathVariable Long userId,
            @RequestParam(name = "active") boolean active) {

        return adminService.updateUserStatus(userId, active);
    }

    // Rental agents for the car form dropdown
    @GetMapping("/api/admin/rental-agent/all")
    public List<RentalAgentResponseDto> getAllRentalAgents() {
        return adminService.getAllRentalAgents();
    }


    // admin home page
    @GetMapping("/api/admin/dashboard/summary")
    public AdminDashboardSummaryDto getDashboardSummary() {
        return adminService.getDashboardSummary();
    }
}
