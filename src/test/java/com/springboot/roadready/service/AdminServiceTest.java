
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.AdminRequestDto;
import com.springboot.roadready.dto.response.AdminUserResponseDto;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.model.Admin;
import com.springboot.roadready.model.User;
import com.springboot.roadready.repository.AdminRepository;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import com.springboot.roadready.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalAgentRepository rentalAgentRepository;

    @Mock
    private ReservationService reservationService;


    // Test 1: Add admin successfully
    @Test
    public void addAdminTest() {

        AdminRequestDto dto = new AdminRequestDto(
                "Babita",
                "9876543210",
                "babita",
                "password123"
        );

        User user = new User();
        user.setUsername("babita");

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        adminService.addAdmin(dto);

        verify(passwordEncoder, times(1))
                .encode("password123");

        verify(userRepository, times(1))
                .save(any(User.class));

        verify(adminRepository, times(1))
                .save(any(Admin.class));

        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(adminRepository);
    }


    // Test 2: Get all users successfully
    @Test
    public void getAllUsersTest() {

        AdminUserResponseDto dto1 = new AdminUserResponseDto(
                1L, "customer1", Role.CUSTOMER, true
        );

        AdminUserResponseDto dto2 = new AdminUserResponseDto(
                2L, "agent1", Role.RENTAL_AGENT, true
        );

        when(userRepository.getUserForAdmin(
                List.of(Role.CUSTOMER, Role.RENTAL_AGENT),
                null,
                null,
                PageRequest.of(0, 5)
        )).thenReturn(new PageImpl<>(List.of(dto1, dto2)));

        var result = adminService.getAllUsers(null, null, 0, 5);

        assertEquals(2, result.list().size());
        assertEquals("customer1", result.list().get(0).username());
        assertEquals("agent1", result.list().get(1).username());
        assertEquals(1, result.paginationInfo().totalPages());
        assertEquals(2L, result.paginationInfo().totalRecords());

        verify(userRepository, times(1))
                .getUserForAdmin(
                        List.of(Role.CUSTOMER, Role.RENTAL_AGENT),
                        null,
                        null,
                        PageRequest.of(0, 5)
                );

        verifyNoMoreInteractions(userRepository);
    }


    // Test 3: Get all users with ADMIN role should throw exception
    @Test
    public void getAllUsersTest_InvalidRole() {

        assertThrows(InvalidCallException.class,
                () -> adminService.getAllUsers(Role.ADMIN, null, 0, 5));

        verifyNoInteractions(userRepository);
    }


    // Test 4: Update user status successfully
    @Test
    public void updateUserStatusTest() {

        User user = new User();
        user.setId(1L);
        user.setUsername("customer1");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        AdminUserResponseDto result =
                adminService.updateUserStatus(1L, false);

        assertEquals(1L, result.id());
        assertEquals("customer1", result.username());
        assertEquals(Role.CUSTOMER, result.role());
        assertFalse(result.active());

        verify(userRepository, times(1))
                .findById(1L);

        verify(userRepository, times(1))
                .save(user);

        verifyNoMoreInteractions(userRepository);
    }


    // Test 5: Update user status when user does not exist
    @Test
    public void updateUserStatusTest_Invalid() {

        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.updateUserStatus(10L, false));

        verify(userRepository, times(1))
                .findById(10L);

        verify(userRepository, times(0))
                .save(any(User.class));

        verifyNoMoreInteractions(userRepository);
    }

}
