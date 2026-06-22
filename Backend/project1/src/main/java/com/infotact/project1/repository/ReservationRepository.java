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

@Repository

// JpaRepository provides built-in CRUD operations
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // Retrieve reservations belonging to a user
    List<Reservation> findByUser(User user);

    // Retrieve reservations by status
    List<Reservation> findByReservationStatus(
            ReservationStatus reservationStatus);

    // Count overlapping active reservations
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
}