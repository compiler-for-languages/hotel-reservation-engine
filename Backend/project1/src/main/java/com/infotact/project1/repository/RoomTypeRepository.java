package com.infotact.project1.repository;

import com.infotact.project1.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
/*
 * Repository responsible for RoomType database operations.
 *
 * Provides CRUD functionality and custom queries related to
 * room categories offered by the hotel.
 *
 * Examples:
 * - Standard Room
 * - Deluxe Room
 * - Suite Room
 *
 * RoomType defines:
 * - Name
 * - Price per night
 * - Capacity
 * - Booking status
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
 * Entity Type : RoomType
 * Primary Key : Long (roomTypeId)
 */
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    /*
     * Retrieves a room type using its unique name.
     *
     * Used to:
     * - Prevent duplicate room type creation
     * - Search room categories by name
     * - Validate room type existence
     *
     * Example:
     * DELUXE
     * SUITE
     * STANDARD
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM room_types
     * WHERE name = ?;
     */
    Optional<RoomType> findByName(String name);
}