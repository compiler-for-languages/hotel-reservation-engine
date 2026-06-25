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

    @OneToOne
    @JoinColumn(name = "reservation_id",
            nullable = false,
            unique = true)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "room_id",
            nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "assigned_by",
            nullable = false)
    private User assignedBy;

    /*
     * When receptionist allotted the room.
     */
    @Column(nullable = false)
    private LocalDateTime assignedAt;

    /*
     * Actual guest arrival time.
     * Set when receptionist presses Check-In.
     */
    private LocalDateTime actualCheckIn;

    /*
     * Actual guest departure time.
     * Set during Check-Out.
     */
    private LocalDateTime actualCheckOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(length = 500)
    private String remarks;
    /*receptionist writes remarks
    Late Check-In
    VIP Guest
     */

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
