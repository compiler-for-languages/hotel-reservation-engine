package com.infotact.project1.repository;

import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Repository responsible for Room database operations.
 *
 * Provides CRUD functionality and custom queries related to:
 * - Room inventory management
 * - Room availability tracking
 * - Room assignment workflow
 * - Room type based searches
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
 * Entity Type : Room
 * Primary Key : Long (roomId)
 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /*
     * Retrieves a room using its room number.
     *
     * Used to prevent duplicate room numbers during
     * room creation and updates.
     *
     * Example:
     * Room Number = A101
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM rooms
     * WHERE room_number = ?;
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /*
     * Retrieves all rooms belonging to a room type.
     *
     * Example:
     * DELUXE
     *   ↓
     * Room 101
     * Room 102
     * Room 103
     *
     * Useful for:
     * - Inventory management
     * - Availability calculations
     * - Room assignment
     */
    List<Room> findByRoomType(RoomType roomType);

    /*
     * Retrieves rooms matching both room type and room status.
     *
     * Example:
     *
     * DELUXE + AVAILABLE
     *        ↓
     * Room 101
     * Room 105
     * Room 110
     *
     * Useful during:
     * - Room assignment
     * - Check-in process
     * - Availability management
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM rooms
     * WHERE room_type_id = ?
     * AND room_status = ?;
     */
    List<Room> findByRoomTypeAndRoomStatus(
            RoomType roomType,
            RoomStatus roomStatus);
    //Spring automatically searches for with this room type and this room status

    /*
     * Counts the total number of rooms
     * belonging to a room type.
     *
     * Example:
     *
     * DELUXE
     *   ↓
     * 15 rooms
     *
     * Used by AvailabilityService.
     *
     * Spring automatically generates:
     *
     * SELECT COUNT(*)
     * FROM rooms
     * WHERE room_type_id = ?;
     */
    long countByRoomType(RoomType roomType);

    long countByRoomStatus(
            RoomStatus roomStatus);
    //used in Reception Service
}