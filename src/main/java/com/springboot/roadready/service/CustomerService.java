package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.CustomerRequestDto;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.mapper.CustomerMapper;
import com.springboot.roadready.model.Customer;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.CustomerRepository;
import com.springboot.roadready.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationService reservationService;

    // add a new customer
    @Transactional
    public void addCustomer(@Valid CustomerRequestDto customerRequestDto) {

        log.info("Adding new customer with username: {}", customerRequestDto.username());

        // Convert DTO to Customer entity
        Customer customer =
                CustomerMapper.convertDtoToEntity(customerRequestDto);

        // Create User object
        User user = new User();
        user.setUsername(customerRequestDto.username());
        user.setPassword(
                passwordEncoder.encode(customerRequestDto.password()));
        user.setRole(Role.CUSTOMER);

        // Save User first
        user = userRepository.save(user);

        // Associate User with Customer
        customer.setUser(user);

        // Save Customer
        customerRepository.save(customer);

        log.info("Customer added successfully with username: {}", customerRequestDto.username());
    }

    // logged-in customer's home page
    public com.springboot.roadready.dto.response.CustomerDashboardSummaryDto getDashboardSummary(
            String customerUsername) {

        log.info("Fetching dashboard summary for customer: {}", customerUsername);

        com.springboot.roadready.dto.response.CustomerDashboardSummaryDto summary =
                reservationService.getCustomerDashboardSummary(customerUsername);

        log.info("Dashboard summary fetched successfully for customer: {}", customerUsername);

        return summary;
    }
}