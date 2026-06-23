package com.infotact.project1.controller;

import com.infotact.project1.dto.request.LoginRequestDTO;
import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.dto.response.LoginResponseDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register a new user account
    @PostMapping("/register")
    public UserResponseDTO register(
            @RequestBody RegisterRequestDTO requestDTO) {

        return authService.register(requestDTO);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(
            @RequestBody LoginRequestDTO requestDTO) {

        return authService.login(requestDTO);
    }
}