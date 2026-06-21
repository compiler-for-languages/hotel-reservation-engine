package com.infotact.project1.model;

import com.infotact.project1.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Represents a room booking made by a customer.
 * Customers reserve a room type while actual room allocation happens during check-in.
 * Acts as the central entity connecting booking, payment, guests and room assignment.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    // Customer who owns this reservation
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Reserved room category (Standard, Deluxe, Suite)
    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    // Used to validate occupancy against RoomType capacity
    @Column(nullable = false)
    private Integer guestCount;

    // Stored as STRING to keep enum values readable in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    // Optional notes or preferences provided by the customer
    @Column(length = 500)
    private String specialRequest;

    // Time when reservation was originally created
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookingTime;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Populate timestamps when reservation is created
    @PrePersist
    public void onCreate() {
        this.bookingTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Update timestamp whenever reservation details change
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}