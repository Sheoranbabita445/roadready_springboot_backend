
package com.springboot.roadready.service;


import com.springboot.roadready.dto.request.AdminRequestDto;
import com.springboot.roadready.dto.response.AdminDashboardSummaryDto;
import com.springboot.roadready.dto.response.AdminUserFilterDto;
import com.springboot.roadready.dto.response.AdminUserResponseDto;
import com.springboot.roadready.dto.response.PaginationDto;
import com.springboot.roadready.dto.response.RentalAgentResponseDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.enums.ReservationStatus;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.mapper.AdminMapper;
import com.springboot.roadready.model.Admin;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.AdminRepository;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import com.springboot.roadready.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarRepository carRepository;
    private final RentalAgentRepository rentalAgentRepository;
    private final ReservationService reservationService;

    @Transactional
    public void addAdmin(@Valid AdminRequestDto adminRequestDto) {

        log.info("Adding new admin with username: {}", adminRequestDto.username());

        Admin admin = AdminMapper.convertDtoToEntity(adminRequestDto);

        //create user object
        User user = new User();
        user.setUsername(adminRequestDto.username());
        user.setPassword(passwordEncoder.encode(adminRequestDto.password()));
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);

        admin.setUser(user);

        adminRepository.save(admin);

        log.info("Admin added successfully with username: {}", adminRequestDto.username());
    }


    // get all users for admin (customers and rental agents)
    public AdminUserFilterDto getAllUsers(
            Role role, String search, Integer page, Integer size) {

        log.info("Fetching users with role: {}, search: {}, page: {}, size: {}",
                role, search, page, size);

        // admins are never listed
        if (role == Role.ADMIN) {
            throw new InvalidCallException("Only customers and rental agents can be listed");
        }

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);
        // search text becomes a lower-case LIKE pattern (null = no search)
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.trim().toLowerCase() + "%";

        // Get customers and rental agents
        Page<AdminUserResponseDto> userPage = userRepository.getUserForAdmin(
                List.of(Role.CUSTOMER, Role.RENTAL_AGENT),
                role,
                searchPattern,
                pageable
        );

        log.info("Successfully fetched {} users", userPage.getContent().size());

        return new AdminUserFilterDto(
                userPage.getContent(),
                new PaginationDto(userPage.getTotalPages(), userPage.getTotalElements(), userPage.getContent().size())
        );
    }


    // activate or deactivate a user account
    public AdminUserResponseDto updateUserStatus(
            Long userId, boolean active) {

        log.info("Updating status for user ID: {} to active: {}", userId, active);

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Update account status
        user.setActive(active);

        // Save updated user
        userRepository.save(user);

        log.info("User status updated successfully for user ID: {}", userId);

        // Return updated user details
        return AdminMapper.convertUserToDto(user);
    }


    // get every rental agent (the admin picks one when adding or editing a car)
    public List<RentalAgentResponseDto> getAllRentalAgents() {

        log.info("Fetching all rental agents");

        List<RentalAgentResponseDto> rentalAgents =
                rentalAgentRepository.getAllRentalAgents();

        log.info("Successfully fetched {} rental agents", rentalAgents.size());

        return rentalAgents;
    }


    // admin home page
    public AdminDashboardSummaryDto getDashboardSummary() {

        log.info("Fetching admin dashboard summary");

        Map<String, Long> carsByStatus = new LinkedHashMap<>();
        for (CarStatus status : CarStatus.values()) {
            carsByStatus.put(status.name(), 0L);
        }
        long totalCars = 0;
        for (Object[] row : carRepository.countCarsByStatus()) {
            long count = ((Number) row[1]).longValue();
            carsByStatus.put(((CarStatus) row[0]).name(), count);
            totalCars += count;
        }

        Map<String, Long> reservationsByStatus = reservationService.countReservationsByStatus();
        long totalReservations = reservationsByStatus.values().stream().mapToLong(Long::longValue).sum();

        log.info("Admin dashboard summary fetched successfully. Total cars: {}, Total reservations: {}",
                totalCars, totalReservations);

        return new AdminDashboardSummaryDto(
                totalCars,
                totalReservations,
                reservationsByStatus.get(ReservationStatus.PENDING.name()),
                reservationsByStatus.get(ReservationStatus.CANCELLATION_REQUESTED.name()),
                userRepository.countByRole(Role.CUSTOMER),
                userRepository.countByRole(Role.RENTAL_AGENT),
                reservationsByStatus,
                carsByStatus
        );
    }

}
