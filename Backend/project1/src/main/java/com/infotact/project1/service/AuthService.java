package com.infotact.project1.service;

import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponseDTO register(RegisterRequestDTO requestDTO) {

        // Prevent duplicate email addresses
        userRepository.findByEmail(requestDTO.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException(
                            "Email already registered: "
                                    + requestDTO.getEmail());
                });

        // Prevent duplicate phone numbers
        userRepository.findByPhone(requestDTO.getPhone())
                .ifPresent(user -> {
                    throw new RuntimeException(
                            "Phone number already registered: "
                                    + requestDTO.getPhone());
                });

        User user = new User();

        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
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