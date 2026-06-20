package com.infotact.project1.service;

import com.infotact.project1.dto.request.RoomPatchRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.dto.response.RoomResponseDTO;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class RoomService {

    // Dependency remains immutable after injection
    private final RoomRepository roomRepository;

    // Dependency remains immutable after injection
    private final RoomTypeRepository roomTypeRepository;

    public RoomResponseDTO createRoom(RoomRequestDTO requestDTO) {

        // Fetch referenced room type before creating room
        RoomType roomType = roomTypeRepository.findById(requestDTO.getRoomTypeId())
                .orElseThrow(() ->
                        new RuntimeException("Room Type not found with id: "
                                + requestDTO.getRoomTypeId()));

        // Prevent duplicate room numbers
        roomRepository.findByRoomNumber(requestDTO.getRoomNumber().trim())
                .ifPresent(room -> {
                    throw new RuntimeException(
                            "Room number already exists: "
                                    + requestDTO.getRoomNumber());
                });

        Room room = new Room();

        room.setRoomNumber(requestDTO.getRoomNumber().trim());
        room.setFloorNumber(requestDTO.getFloorNumber());
        room.setRoomStatus(requestDTO.getRoomStatus());
        room.setRoomType(roomType);

        Room savedRoom = roomRepository.save(room);

        return mapToResponse(savedRoom);
    }

    public List<RoomResponseDTO> getAllRooms() {

        // Stream API for DTO conversion
        return roomRepository.findAll()
                .stream()

                // Method reference improves readability
                .map(this::mapToResponse)
                .toList();
    }

    public RoomResponseDTO getRoomById(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException("Room not found with id: " + roomId));

        return mapToResponse(room);
    }

    // Retrieve room using room number
    // Custom repository method
    public RoomResponseDTO getRoomByRoomNumber(String roomNumber) {

        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() ->
                        new RuntimeException("Room not found with room number: "
                                + roomNumber));

        return mapToResponse(room);
    }

    // Retrieve all rooms under a specific room type
    // Custom repository method
    public List<RoomResponseDTO> getRoomsByRoomType(Long roomTypeId) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() ->
                        new RuntimeException("Room Type not found with id: "
                                + roomTypeId));

        // Stream API for DTO conversion
        return roomRepository.findByRoomType(roomType)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    //filter based on particular room type and particular room status
    public List<RoomResponseDTO> getRoomsByRoomTypeAndStatus(
            Long roomTypeId,
            RoomStatus roomStatus) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)

                // Prevents querying with invalid room type
                .orElseThrow(() ->
                        new RuntimeException(
                                "Room Type not found. Please provide a valid room type id."));

        // Stream API for DTO conversion
        return roomRepository.findByRoomTypeAndRoomStatus(
                        roomType,
                        roomStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public void deleteRoom(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException("Room not found with id: " + roomId));

        roomRepository.delete(room);
    }

    // Partial room update
    public RoomResponseDTO updateRoom(
            Long roomId,
            RoomPatchRequestDTO requestDTO) {

        Room room = roomRepository.findById(roomId)

                // Prevents updates on non-existent records
                .orElseThrow(() ->
                        new RuntimeException("Room not found with id: " + roomId));

        if (requestDTO.getRoomNumber() != null) {

            // Prevent duplicate room numbers during update
            roomRepository.findByRoomNumber(requestDTO.getRoomNumber().trim())
                    .ifPresent(existingRoom -> {

                        if (!existingRoom.getRoomId()
                                .equals(room.getRoomId())) {

                            throw new RuntimeException(
                                    "Room number already exists: "
                                            + requestDTO.getRoomNumber());
                        }
                    });

            room.setRoomNumber(requestDTO.getRoomNumber().trim());
        }

        if (requestDTO.getRoomStatus() != null) {
            room.setRoomStatus(requestDTO.getRoomStatus());
        }

        Room updatedRoom = roomRepository.save(room);

        return mapToResponse(updatedRoom);
    }

    // Entity → DTO mapper
    private RoomResponseDTO mapToResponse(Room room) {

        // Builder pattern improves object creation readability
        return RoomResponseDTO.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomTypeName(room.getRoomType().getName())
                .floorNumber(room.getFloorNumber())
                .roomStatus(room.getRoomStatus())
                .build();
    }
}