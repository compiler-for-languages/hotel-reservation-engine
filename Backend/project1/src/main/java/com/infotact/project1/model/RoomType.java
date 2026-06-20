package com.infotact.project1.model;

import com.infotact.project1.enums.RoomTypeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Represents a room category offered by the hotel.
 * Customers book room types while actual rooms are assigned during check-in.
 * Examples: Standard, Deluxe, Suite.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTypeId;

    // Display name shown to customers
    @Column(nullable = false, unique = true)
    private String name;

    // Detailed description of the room category
    @Column(length = 500)
    private String description;

    // Base price charged per night
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;/*Double -> Precision issues
                                       BigDecimal -> Accurate financial calculations
    */

    // Maximum guests allowed for this room type
    @Column(nullable = false)
    private Integer capacity;

    // Controls whether this room type can be booked
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomTypeStatus status;


    // Automatically managed audit timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Populate timestamps when room type is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Update timestamp whenever room type is modified
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}