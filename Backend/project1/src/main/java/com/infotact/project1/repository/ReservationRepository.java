package com.infotact.project1.repository;

import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/*
 * Repository responsible for Reservation database operations.
 *
 * Provides CRUD functionality and custom queries related to:
 * - Customer bookings
 * - Reservation tracking
 * - Availability checking
 * - Overbooking prevention
 */
@Repository

/*
 * JpaRepository provides:
 *
 * save()
 * findById()
 * findAll()
 * delete()
 * deleteById()
 * existsById()
 *
 * Entity Type : Reservation
 * Primary Key : Long (reservationId)
 */
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // Retrieve reservations belonging to a user
    /*
     * Example:
     * User #5
     *    ↓
     * Reservation #10
     * Reservation #15
     * Reservation #22
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM reservations
     * WHERE user_id = ?;
     */
    List<Reservation> findByUser(User user);


    // Retrieve reservations by status
    /*
     * Examples:
     * PENDING
     * CONFIRMED
     * CANCELLED
     * COMPLETED
     * EXPIRED
     *
     * Useful for:
     * - Admin dashboards
     * - Booking management
     * - Reservation monitoring
     */
    List<Reservation> findByReservationStatus(
            ReservationStatus reservationStatus);

    /*
     * Counts reservations that overlap with a requested date range.
     *
     * Purpose:
     * Prevent overbooking of a room type.
     *
     * Used by AvailabilityService.
     *
     * Returns:
     * Number of active reservations already occupying
     * the requested room type during the requested dates.
     */
    @Query("""
            SELECT COUNT(r)
            FROM Reservation r
            WHERE r.roomType.roomTypeId = :roomTypeId
            AND r.reservationStatus NOT IN (
                    com.infotact.project1.enums.ReservationStatus.CANCELLED,
                    com.infotact.project1.enums.ReservationStatus.EXPIRED
            )
            AND r.checkInDate < :checkOutDate
            AND r.checkOutDate > :checkInDate
            """)
    long countOverlappingReservations(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    // Power of Spring Data JPA to create SQL, open connection, execute query,
    // fetch result, close connection, return value

    List<Reservation> findByReservationStatusAndCheckInDate(
            ReservationStatus status,
            LocalDate checkInDate);

    List<Reservation> findByReservationStatusAndCheckOutDate(
            ReservationStatus status,
            LocalDate checkOutDate);
}