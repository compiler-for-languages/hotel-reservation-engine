package com.infotact.project1.model;

import com.infotact.project1.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * Represents the allocation of a physical room to a reservation.
 * Created during check-in by the receptionist.
 * Bridges the gap between room reservation and actual room occupancy.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_assignments")
public class RoomAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    // Reservation receiving the room allocation
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    // Physical room assigned to the reservation
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Receptionist/Admin who performed the assignment
    @ManyToOne
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    // Time when the room was allotted
    @Column(nullable = false)
    private LocalDateTime assignedAt;

    // Tracks the room assignment lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    // Automatically managed audit timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Populate timestamps when assignment is created
    @PrePersist
    public void onCreate() {
        this.assignedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
