package com.infotact.project1.repository;

import com.infotact.project1.model.BookingHold;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/*
 * Repository responsible for managing Booking Hold records in Redis.
 *
 * Booking holds are temporary reservations created before payment
 * completion to prevent multiple users from booking the same room.
 *
 * Data stored here is short-lived and automatically expires
 * using Redis TTL (Time To Live).
 */
@Repository

/*
 * CrudRepository provides built-in Redis operations:
 *
 * save()
 * findById()
 * existsById()
 * deleteById()
 * delete()
 *
 * Key Type    : String (holdId)
 * Value Type  : BookingHold
 */
// CrudRepository provides Redis CRUD operations
public interface BookingHoldRepository
        extends CrudRepository<BookingHold, String> {

}