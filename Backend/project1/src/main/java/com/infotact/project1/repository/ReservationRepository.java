package com.infotact.project1.repository;

import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}