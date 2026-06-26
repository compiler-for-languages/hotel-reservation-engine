package com.infotact.project1.repository;

import com.infotact.project1.enums.AssignmentStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Repository responsible for RoomAssignment database operations.
 *
 * Handles room allocation records created by the receptionist
 * during the guest check-in process.
 */

@Repository

// JpaRepository provides built-in CRUD operations
public interface RoomAssignmentRepository
        extends JpaRepository<RoomAssignment, Long> {

    /*
     * Retrieves the room assignment associated with
     * a particular reservation.
     *
     * One reservation can have only one room assignment.
     */
    Optional<RoomAssignment> findByReservation(
            Reservation reservation);

    /*
     * Retrieves all assignments associated with
     * a physical room.
     *
     * Useful for occupancy history.
     */
    List<RoomAssignment> findByRoom(
            Room room);

    /*
     * Retrieves assignments by lifecycle status.
     *
     * Example:
     * ASSIGNED
     * CHECKED_IN
     * CHECKED_OUT
     */
    List<RoomAssignment> findByStatus(
            AssignmentStatus status);

    /*
     * Checks whether a reservation has already
     * been assigned a room.
     *
     * Prevents assigning multiple rooms
     * to the same reservation.
     */
    boolean existsByReservation(
            Reservation reservation);
}