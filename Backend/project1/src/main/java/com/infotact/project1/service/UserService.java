package com.infotact.project1.service;

import com.infotact.project1.dto.request.UserPatchRequestDTO;
import com.infotact.project1.dto.request.UserRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import com.infotact.project1.exception.BusinessExceptions;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(UserRequestDTO requestDTO) {

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
        user.setEmail(requestDTO.getEmail());
        user.setPhone(requestDTO.getPhone());
        user.setGender(requestDTO.getGender());
        user.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(requestDTO.getRole());
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessExceptions.userNotFound(userId));
        return mapToResponse(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessExceptions.userNotFoundByEmail(email));
        return mapToResponse(user);
    }

    public UserResponseDTO updateUser(Long userId, UserPatchRequestDTO requestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessExceptions.userNotFound(userId));

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
                        if (!existingUser.getUserId().equals(user.getUserId())) {
                            throw BusinessExceptions.phoneAlreadyExists(requestDTO.getPhone());
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
                .orElseThrow(() -> BusinessExceptions.userNotFound(userId));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userRepository.findByEmail(authentication.getName())
                    .orElse(null);
            if (currentUser != null
                    && currentUser.getRole() == Role.ADMIN
                    && currentUser.getUserId().equals(userId)) {
                throw new RuntimeException("Administrators cannot delete their own account.");
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

    private void authorizeUserUpdate(
            Long targetUserId,
            UserPatchRequestDTO requestDTO,
            User targetUser) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(BusinessExceptions::userNotFound);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (isAdmin) {
            if (currentUser.getUserId().equals(targetUserId)
                    && requestDTO.getAccountStatus() != null
                    && requestDTO.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new RuntimeException("Administrators cannot deactivate their own account.");
            }
            return;
        }

        if (!currentUser.getUserId().equals(targetUserId)) {
            throw BusinessExceptions.accessDenied();
        }

        if (requestDTO.getAccountStatus() != null
                && requestDTO.getAccountStatus() != targetUser.getAccountStatus()) {
            throw BusinessExceptions.accessDenied();
        }
    }
}
