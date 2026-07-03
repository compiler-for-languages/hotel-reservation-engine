package com.infotact.project1.service;

import com.infotact.project1.dto.request.UserPatchRequestDTO;
import com.infotact.project1.dto.request.UserRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class UserService {

    // Dependency remains immutable after injection
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


// Admin creates receptionist and receptionist creates walk-in user
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {

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
        user.setEmail(requestDTO.getEmail());
        user.setPhone(requestDTO.getPhone());
        user.setGender(requestDTO.getGender());

        // BCrypt hashes password before storing
        user.setPasswordHash(
                passwordEncoder.encode(requestDTO.getPassword()));

        user.setRole(requestDTO.getRole());

        user.setAccountStatus(
                com.infotact.project1.enums.AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public List<UserResponseDTO> getAllUsers() {

        // Stream API for DTO conversion
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponseDTO getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        return mapToResponse(user);
    }

    // Custom repository method
    public UserResponseDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        return mapToResponse(user);
    }

    // Partial user update
    public UserResponseDTO updateUser(
            Long userId,
            UserPatchRequestDTO requestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        authorizeUserUpdate(userId, requestDTO, user);

        if (requestDTO.getFirstName() != null) {
            user.setFirstName(requestDTO.getFirstName());
        }

        if (requestDTO.getLastName() != null) {
            user.setLastName(requestDTO.getLastName());
        }

        if (requestDTO.getPhone() != null) {

            userRepository.findByPhone(requestDTO.getPhone())
                    .ifPresent(existingUser -> {

                        if (!existingUser.getUserId()
                                .equals(user.getUserId())) {

                            throw new RuntimeException("PHONE_ALREADY_EXISTS");
                        }
                    });

            user.setPhone(requestDTO.getPhone());
        }

        if (requestDTO.getAccountStatus() != null) {
            user.setAccountStatus(requestDTO.getAccountStatus());
        }

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        // Prevent admin from deleting their own account
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userRepository.findByEmail(authentication.getName())
                    .orElse(null);
            if (currentUser != null
                    && currentUser.getRole() == Role.ADMIN
                    && currentUser.getUserId().equals(userId)) {
                throw new RuntimeException("SELF_DELETE_NOT_ALLOWED");
            }
        }

        userRepository.delete(user);
    }

    public List<UserResponseDTO> getAllCustomers() {

        return userRepository.findByRole(Role.CUSTOMER)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<UserResponseDTO> getAllReceptionists() {

        return userRepository.findByRole(Role.RECEPTIONIST)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // Entity → DTO mapper
    private UserResponseDTO mapToResponse(User user) {

        // Builder pattern improves object creation readability
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

    private void authorizeUserUpdate(
            Long targetUserId,
            UserPatchRequestDTO requestDTO,
            User targetUser) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("AUTHENTICATION_REQUIRED");
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (isAdmin) {
            if (currentUser.getUserId().equals(targetUserId)
                    && requestDTO.getAccountStatus() != null
                    && requestDTO.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new RuntimeException("SELF_DEACTIVATE_NOT_ALLOWED");
            }
            return;
        }

        if (!currentUser.getUserId().equals(targetUserId)) {
            throw new RuntimeException("ACCESS_DENIED");
        }

        if (requestDTO.getAccountStatus() != null
                && requestDTO.getAccountStatus() != targetUser.getAccountStatus()) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }
}
