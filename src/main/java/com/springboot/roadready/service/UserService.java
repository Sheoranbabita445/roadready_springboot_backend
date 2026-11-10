
package com.springboot.roadready.service;

import com.springboot.roadready.dto.response.UserDetailsDto;
import com.springboot.roadready.exception.InactiveUserException;
import com.springboot.roadready.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserDetailsDto getUserDetails(String loggedInUsername) {

        log.info("Fetching user details for username: {}", loggedInUsername);

        UserDetailsDto detailsDto = userRepository.getUserDetails(loggedInUsername);

        if (!detailsDto.isActive()) {
            log.info("User account is inactive for username: {}", loggedInUsername);
            throw new InactiveUserException("User is disabled");
        }

        log.info("Successfully fetched user details for username: {}", loggedInUsername);

        return detailsDto;
    }
}
