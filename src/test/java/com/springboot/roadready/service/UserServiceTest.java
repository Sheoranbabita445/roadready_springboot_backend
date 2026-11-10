
package com.springboot.roadready.service;

import com.springboot.roadready.dto.response.UserDetailsDto;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.exception.InactiveUserException;
import com.springboot.roadready.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;


    // Test 1: Get user details when user is active
    @Test
    public void getUserDetailsTest() {

        UserDetailsDto dto = new UserDetailsDto(
                "customer1",
                true,
                Role.CUSTOMER
        );

        when(userRepository.getUserDetails("customer1"))
                .thenReturn(dto);

        UserDetailsDto result = userService.getUserDetails("customer1");

        assertNotNull(result);
        assertEquals("customer1", result.Username());
        assertTrue(result.isActive());
        assertEquals(Role.CUSTOMER, result.role());

        verify(userRepository, times(1)).getUserDetails("customer1");
        verifyNoMoreInteractions(userRepository);
    }


    // Test 2: Throw exception when user is inactive
    @Test
    public void getUserDetailsTest_InactiveUser() {

        UserDetailsDto dto = new UserDetailsDto(
                "customer1",
                false,
                Role.CUSTOMER
        );

        when(userRepository.getUserDetails("customer1"))
                .thenReturn(dto);

        assertThrows(InactiveUserException.class, () -> {
            userService.getUserDetails("customer1");
        });

        verify(userRepository, times(1)).getUserDetails("customer1");
        verifyNoMoreInteractions(userRepository);
    }
}
