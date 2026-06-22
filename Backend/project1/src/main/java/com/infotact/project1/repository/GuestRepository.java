package com.infotact.project1.repository;

import com.infotact.project1.model.Guest;
import com.infotact.project1.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

// JpaRepository provides built-in CRUD operations
public interface GuestRepository
        extends JpaRepository<Guest, Long> {

    // Retrieve guests belonging to a reservation
    List<Guest> findByReservation(Reservation reservation);
}