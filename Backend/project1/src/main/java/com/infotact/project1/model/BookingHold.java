package com.infotact.project1.model;

import com.infotact.project1.enums.BookingHoldStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Represents a temporary reservation lock before payment completion.
 * Prevents multiple users from booking the same room type simultaneously.
 * Converts into a Reservation after successful payment.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking_holds")
public class BookingHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdId;

    // Customer who initiated the booking process
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Room category being temporarily reserved
    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    // Determines when the temporary hold becomes invalid
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Tracks the lifecycle of the booking hold
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingHoldStatus status;

    // Automatically managed audit timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Populate timestamp when hold is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}