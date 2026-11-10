
package com.springboot.roadready.service;

import com.springboot.roadready.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Test
    public void loadUserByUsernameTest() {

        when(userRepository.loadUserByUsername("customer1"))
                .thenReturn(userDetails);

        UserDetails result =
                myUserDetailsService.loadUserByUsername("customer1");

        assertNotNull(result);
        assertSame(userDetails, result);

        verify(userRepository, times(1))
                .loadUserByUsername("customer1");

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void loadUserByUsernameUserNotFoundTest() {

        when(userRepository.loadUserByUsername("unknown"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () ->
                myUserDetailsService.loadUserByUsername("unknown"));

        verify(userRepository, times(1))
                .loadUserByUsername("unknown");

        verifyNoMoreInteractions(userRepository);
    }
}
