package com.infotact.project1.service;

import com.infotact.project1.dto.request.LoginRequestDTO;
import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.dto.response.LoginResponseDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import com.infotact.project1.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerCustomer(RegisterRequestDTO requestDTO) {

        // Prevent duplicate email addresses
        userRepository.findByEmail(requestDTO.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("EMAIL_ALREADY_EXISTS");
                });

        // Prevent duplicate phone numbers
        userRepository.findByPhone(requestDTO.getPhone())
                .ifPresent(user -> {
                    throw new RuntimeException("PHONE_ALREADY_EXISTS");
                });

        User user = new User();

        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        user.setGender(requestDTO.getGender());
        user.setEmail(requestDTO.getEmail());
        user.setPhone(requestDTO.getPhone());

        // BCrypt hashes password before storing
        user.setPasswordHash(
                passwordEncoder.encode(requestDTO.getPassword()));

        // Self registration creates normal users only
        user.setRole(Role.CUSTOMER);

        // Newly registered account is active
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }


    public LoginResponseDTO login(
            LoginRequestDTO requestDTO) {

        User user = userRepository.findByEmail(
                        requestDTO.getEmail())

                .orElseThrow(() ->
                        new RuntimeException("INVALID_CREDENTIALS"));

        if (user.getAccountStatus()
                != AccountStatus.ACTIVE) {

            throw new RuntimeException("ACCOUNT_INACTIVE");
        }

        if (!passwordEncoder.matches(
                requestDTO.getPassword(),   // Once again , while checking , password is encrypted by Bcrypt, there is no concept of decrypting
                user.getPasswordHash())) {

            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        String token =
                jwtService.generateToken(

                        user);

        return new LoginResponseDTO(token);
    }

    public UserResponseDTO getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("AUTHENTICATION_REQUIRED");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        return mapToResponse(user);
    }


    // Entity → DTO mapper
    private UserResponseDTO mapToResponse(User user) {

        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .build();
    }
}