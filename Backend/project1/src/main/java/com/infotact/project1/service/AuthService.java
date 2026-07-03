package com.infotact.project1.service;

import com.infotact.project1.dto.request.LoginRequestDTO;
import com.infotact.project1.dto.request.ProfilePatchRequestDTO;
import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.dto.response.LoginResponseDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import com.infotact.project1.exception.BusinessExceptions;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import com.infotact.project1.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO registerCustomer(RegisterRequestDTO requestDTO) {

        userRepository.findByEmail(requestDTO.getEmail())
                .ifPresent(user -> {
                    throw BusinessExceptions.emailAlreadyExists(requestDTO.getEmail());
                });

        userRepository.findByPhone(requestDTO.getPhone())
                .ifPresent(user -> {
                    throw BusinessExceptions.phoneAlreadyExists(requestDTO.getPhone());
                });

        User user = new User();

        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        user.setGender(requestDTO.getGender());
        user.setEmail(requestDTO.getEmail());
        user.setPhone(requestDTO.getPhone());
        user.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(BusinessExceptions::invalidCredentials);

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw BusinessExceptions.accountInactive();
        }

        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPasswordHash())) {
            throw BusinessExceptions.invalidCredentials();
        }

        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token);
    }

    public UserResponseDTO getCurrentUser() {

        return mapToResponse(getAuthenticatedUser());
    }

    public UserResponseDTO updateProfile(ProfilePatchRequestDTO requestDTO) {

        User user = getAuthenticatedUser();

        if (requestDTO.getFirstName() != null) {
            user.setFirstName(requestDTO.getFirstName());
        }

        if (requestDTO.getLastName() != null) {
            user.setLastName(requestDTO.getLastName());
        }

        if (requestDTO.getPhone() != null) {
            userRepository.findByPhone(requestDTO.getPhone())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getUserId().equals(user.getUserId())) {
                            throw BusinessExceptions.phoneAlreadyExists(requestDTO.getPhone());
                        }
                    });
            user.setPhone(requestDTO.getPhone());
        }

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw BusinessExceptions.authenticationRequired();
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(BusinessExceptions::userNotFound);
    }

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
