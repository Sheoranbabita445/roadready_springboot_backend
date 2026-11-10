package com.springboot.roadready.controller;


import com.springboot.roadready.dto.response.TokenDto;
import com.springboot.roadready.dto.response.UserDetailsDto;
import com.springboot.roadready.service.UserService;
import com.springboot.roadready.utility.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final JwtUtility jwtUtility;
    private final UserService userService;

    @GetMapping("/api/auth/login")
    public TokenDto login(Principal principal){

        String token = jwtUtility.generateToken(principal.getName());
        return new TokenDto(
                token,
                jwtUtility.extractExpiration(token),
                LocalDateTime.now().toString()
        );
    }

    @GetMapping("/api/auth/user-details")
    public UserDetailsDto getUserDetails(Principal principal){
        String loggedInUsername = principal.getName();
        return userService.getUserDetails(loggedInUsername);
    }


}