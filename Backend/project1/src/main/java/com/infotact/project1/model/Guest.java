package com.infotact.project1.model;

import com.infotact.project1.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Represents a guest staying under a reservation.
 * A reservation may contain one or more guests.
 * Guest records are typically captured during check-in.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;

    // Reservation under which the guest is staying
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private String firstName;

    private String lastName;

    private String phone;

    // Stored as STRING to keep enum values readable in the database
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dateOfBirth;

    // Automatically managed audit timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Populate timestamp when guest record is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}