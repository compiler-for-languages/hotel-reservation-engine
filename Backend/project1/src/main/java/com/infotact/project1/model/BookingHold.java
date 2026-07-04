package com.infotact.project1.model;

import com.infotact.project1.enums.BookingHoldStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Represents a temporary reservation hold stored in Redis.
 * Prevents multiple users from reserving the same room type simultaneously.
 * Automatically expires after the configured TTL.
 * Converts into a Reservation after successful payment.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor

// Stored in Redis for 5 minutes
@RedisHash(value = "bookingHold", timeToLive = 60)
public class BookingHold {
    @Id
    private String holdId;

    // Customer who initiated the booking
    private Long userId;

    // Room type being temporarily reserved
    private Long roomTypeId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    // Time when the hold becomes invalid
    private LocalDateTime expiresAt;

    // Tracks the lifecycle of the hold
    private BookingHoldStatus status;

    // Audit timestamp
    private LocalDateTime createdAt;

    // Reservation associated with this booking hold
    private Long reservationId;
}