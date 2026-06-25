package com.infotact.project1.repository;

import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

// JpaRepository provides built-in CRUD operations
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Used to validate duplicate room numbers
    Optional<Room> findByRoomNumber(String roomNumber);

    // Used to retrieve rooms belonging to a room type
    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByRoomTypeAndRoomStatus(
            RoomType roomType,
            RoomStatus roomStatus);
    //Spring automatically searches for with this room type and this room status

    long countByRoomType(RoomType roomType);

    long countByRoomStatus(
            RoomStatus roomStatus);
    //used in Reception Service
}