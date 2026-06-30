package com.infotact.project1.model;

import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/*
 * Stores all authenticated users of the hotel reservation system.
 * Supports Customers, Receptionists and Admins.
 * Email is used for login and password is stored as a hashed value.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;


    // Stored as STRING to keep enum values readable in the database
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // Email acts as the unique login identifier
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    // Stores encrypted password (BCrypt hash)
    @Column(nullable = false)
    private String passwordHash;

    // Links user to Role enum for role-based access control
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Restricts account state to predefined enum values
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    // Automatically managed audit timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Populate timestamps when a new user is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Update timestamp whenever user details are modified
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}