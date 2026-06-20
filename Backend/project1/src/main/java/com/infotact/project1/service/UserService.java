package com.infotact.project1.service;

import com.infotact.project1.dto.request.UserPatchRequestDTO;
import com.infotact.project1.dto.request.UserRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class UserService {

    // Dependency remains immutable after injection
    private final UserRepository userRepository;

    public UserResponseDTO createUser(UserRequestDTO requestDTO) {

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

        // Password encryption will be added with Spring Security
        user.setPasswordHash(requestDTO.getPassword());

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
                        new RuntimeException(
                                "User not found with id: " + userId));

        return mapToResponse(user);
    }

    // Custom repository method
    public UserResponseDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email));

        return mapToResponse(user);
    }

    // Partial user update
    public UserResponseDTO updateUser(
            Long userId,
            UserPatchRequestDTO requestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId));

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

                            throw new RuntimeException(
                                    "Phone number already registered: "
                                            + requestDTO.getPhone());
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
                        new RuntimeException(
                                "User not found with id: " + userId));

        userRepository.delete(user);
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
}
