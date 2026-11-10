package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.RentalAgentRequestDto;
import com.springboot.roadready.dto.response.RentalAgentDashboardSummaryDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.mapper.RentalAgentMapper;
import com.springboot.roadready.model.RentalAgent;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import com.springboot.roadready.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalAgentService {

    private final RentalAgentRepository rentalAgentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarRepository carRepository;
    private final ReservationService reservationService;

    @Transactional
    public void addRentalAgent(@Valid RentalAgentRequestDto rentalAgentRequestDto) {

        log.info("Adding new rental agent with username: {}", rentalAgentRequestDto.username());

        RentalAgent rentalAgent = RentalAgentMapper.convertDtoToEntity(rentalAgentRequestDto);

        User user = new User();
        user.setUsername(rentalAgentRequestDto.username());
        user.setPassword(passwordEncoder.encode(rentalAgentRequestDto.password()));
        user.setRole(Role.RENTAL_AGENT);
        user = userRepository.save(user);

        rentalAgent.setUser(user);

        rentalAgentRepository.save(rentalAgent);

        log.info("Rental agent added successfully with username: {}", rentalAgentRequestDto.username());
    }


    // Numbers for the logged-in rental agent's home page: only their own cars and the reservations on those cars
    public RentalAgentDashboardSummaryDto getDashboardSummary(String agentUsername) {

        log.info("Fetching dashboard summary for rental agent: {}", agentUsername);

        Map<String, Long> carsByStatus = new LinkedHashMap<>();
        for (CarStatus status : CarStatus.values()) {
            carsByStatus.put(status.name(), 0L);
        }

        long assignedCars = 0;
        for (Object[] row : carRepository.countCarsByStatusForAgent(agentUsername)) {
            long count = ((Number) row[1]).longValue();
            carsByStatus.put(((CarStatus) row[0]).name(), count);
            assignedCars += count;
        }

        Map<String, Long> reservationsByStatus =
                reservationService.countReservationsByStatusForAgent(agentUsername);

        long totalReservations = reservationsByStatus.values().stream().mapToLong(Long::longValue).sum();

        log.info("Dashboard summary fetched successfully for rental agent: {}. Assigned cars: {}, Total reservations: {}",
                agentUsername, assignedCars, totalReservations);

        return new RentalAgentDashboardSummaryDto(
                assignedCars,
                totalReservations,
                carsByStatus,
                reservationsByStatus
        );
    }
}