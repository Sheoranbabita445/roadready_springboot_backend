
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.CustomerRequestDto;
import com.springboot.roadready.dto.response.CustomerDashboardSummaryDto;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.model.Customer;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.CustomerRepository;
import com.springboot.roadready.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ReservationService reservationService;


    // Test 1: Add customer successfully
    @Test
    public void addCustomerTest() {

        CustomerRequestDto dto = new CustomerRequestDto(
                "Babita",
                "Sheoran",
                "babita@gmail.com",
                "9876543210",
                "Mumbai",
                "babita",
                "password123"
        );

        User user = new User();
        user.setUsername("babita");

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        customerService.addCustomer(dto);

        verify(passwordEncoder, times(1))
                .encode("password123");

        verify(userRepository, times(1))
                .save(any(User.class));

        verify(customerRepository, times(1))
                .save(any(Customer.class));

        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(customerRepository);
    }


    // Test 2: Check user details while adding customer
    @Test
    public void addCustomerTest_UserDetails() {

        CustomerRequestDto dto = new CustomerRequestDto(
                "Babita",
                "Sheoran",
                "babita@gmail.com",
                "9876543210",
                "Mumbai",
                "babita",
                "password123"
        );

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        customerService.addCustomer(dto);

        verify(userRepository, times(1))
                .save(argThat(user ->
                        user.getUsername().equals("babita")
                                && user.getPassword().equals("encodedPassword")
                                && user.getRole() == Role.CUSTOMER
                ));

        verify(customerRepository, times(1))
                .save(argThat(customer ->
                        customer.getFirstName().equals("Babita")
                                && customer.getLastName().equals("Sheoran")
                                && customer.getEmail().equals("babita@gmail.com")
                ));

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }


    // Test 3: Get customer dashboard summary successfully
    @Test
    public void getDashboardSummaryTest() {

        CustomerDashboardSummaryDto summary =
                new CustomerDashboardSummaryDto(
                        2L,
                        Map.of(
                                "CONFIRMED", 1L,
                                "PENDING", 1L
                        ),
                        List.of()
                );

        when(reservationService.getCustomerDashboardSummary("babita"))
                .thenReturn(summary);

        CustomerDashboardSummaryDto result =
                customerService.getDashboardSummary("babita");

        assertNotNull(result);
        assertEquals(2L, result.totalReservations());
        assertEquals(1L, result.reservationsByStatus().get("CONFIRMED"));
        assertEquals(1L, result.reservationsByStatus().get("PENDING"));
        assertEquals(summary, result);

        verify(reservationService, times(1))
                .getCustomerDashboardSummary("babita");

        verifyNoMoreInteractions(reservationService);
    }

}
