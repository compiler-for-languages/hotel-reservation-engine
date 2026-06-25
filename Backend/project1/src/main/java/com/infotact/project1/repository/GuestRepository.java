package com.infotact.project1.repository;

import com.infotact.project1.model.Guest;
import com.infotact.project1.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Repository responsible for Guest database operations.
 *
 * Provides CRUD functionality for guest records
 * and custom query methods related to reservations.
 *
 * Each guest belongs to a reservation and represents
 * an individual staying under that booking.
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
 * Entity Type : Guest
 * Primary Key : Long (guestId)
 */
public interface GuestRepository
        extends JpaRepository<Guest, Long> {

    /*
     * Retrieves all guests associated with a reservation.
     *
     * Example:
     * Reservation #25
     *      ↓
     * Guest 1
     * Guest 2
     * Guest 3
     *
     * Spring Data JPA automatically generates:
     *
     * SELECT *
     * FROM guests
     * WHERE reservation_id = ?
     */
    List<Guest> findByReservation(Reservation reservation);
}