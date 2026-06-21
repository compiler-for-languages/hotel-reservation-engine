package com.infotact.project1.controller;

import com.infotact.project1.dto.request.UserPatchRequestDTO;
import com.infotact.project1.dto.request.UserRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Create a new user
    @PostMapping("/save")
    public UserResponseDTO createUser(
            @RequestBody UserRequestDTO requestDTO) {

        return userService.createUser(requestDTO);
    }

    // Retrieve all users
    @GetMapping("/getall")
    public List<UserResponseDTO> getAllUsers() {

        return userService.getAllUsers();
    }

    // Retrieve user by id
    @GetMapping("/get/{userId}")
    public UserResponseDTO getUserById(
            @PathVariable Long userId) {

        return userService.getUserById(userId);
    }

    // Retrieve user using email
    @GetMapping("/get")
    public UserResponseDTO getUserByEmail(
            @RequestParam String email) {

        return userService.getUserByEmail(email);
    }

    // Partially update user
    @PatchMapping("/update/{userId}")
    public UserResponseDTO updateUser(
            @PathVariable Long userId,
            @RequestBody UserPatchRequestDTO requestDTO) {

        return userService.updateUser(userId, requestDTO);
    }

    // Delete user by id
    @DeleteMapping("/delete/{userId}")
    public String deleteUser(
            @PathVariable Long userId) {

        userService.deleteUser(userId);

        return "User deleted successfully";
    }
}
