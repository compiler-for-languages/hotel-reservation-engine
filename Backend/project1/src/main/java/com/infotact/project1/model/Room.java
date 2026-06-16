package com.infotact.project1.model;

import com.infotact.project1.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * Represents a physical room in the hotel.
 * Customers book room types, while actual rooms are assigned during check-in.
 * Room status controls whether a room can be allotted to guests.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    // Room number displayed to reception staff and guests
    @Column(nullable = false, unique = true)
    private String roomNumber;

    // Links room to its category (Standard_AC, Deluxe_AC, Suite)
    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    private Integer floorNumber;

    // Stored as STRING to keep enum values readable in the database
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus roomStatus;

    // Automatically managed audit timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Populate timestamps when room is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Update timestamp whenever room details change
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}