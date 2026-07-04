package com.infotact.project1.service;

import com.infotact.project1.dto.request.RoomPatchRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.dto.response.RoomResponseDTO;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.exception.BusinessExceptions;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public RoomResponseDTO createRoom(RoomRequestDTO requestDTO) {

        RoomType roomType = roomTypeRepository.findById(requestDTO.getRoomTypeId())
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFound(requestDTO.getRoomTypeId()));

        roomRepository.findByRoomNumber(requestDTO.getRoomNumber().trim())
                .ifPresent(room -> {
                    throw BusinessExceptions.roomNumberExists(requestDTO.getRoomNumber().trim());
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
        return roomRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoomResponseDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> BusinessExceptions.roomNotFound(roomId));
        return mapToResponse(room);
    }

    public RoomResponseDTO getRoomByRoomNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> BusinessExceptions.roomNotFoundByNumber(roomNumber));
        return mapToResponse(room);
    }

    public List<RoomResponseDTO> getRoomsByRoomType(Long roomTypeId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFound(roomTypeId));

        return roomRepository.findByRoomType(roomType)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RoomResponseDTO> getRoomsByRoomTypeAndStatus(
            Long roomTypeId,
            RoomStatus roomStatus) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(BusinessExceptions::roomTypeNotFoundForFilter);

        return roomRepository.findByRoomTypeAndRoomStatus(roomType, roomStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> BusinessExceptions.roomNotFound(roomId));
        roomRepository.delete(room);
    }

    public RoomResponseDTO updateRoom(Long roomId, RoomPatchRequestDTO requestDTO) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> BusinessExceptions.roomNotFound(roomId));

        if (requestDTO.getRoomNumber() != null) {
            roomRepository.findByRoomNumber(requestDTO.getRoomNumber().trim())
                    .ifPresent(existingRoom -> {
                        if (!existingRoom.getRoomId().equals(room.getRoomId())) {
                            throw BusinessExceptions.roomNumberExists(requestDTO.getRoomNumber().trim());
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

    private RoomResponseDTO mapToResponse(Room room) {
        return RoomResponseDTO.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomTypeName(room.getRoomType().getName())
                .floorNumber(room.getFloorNumber())
                .roomStatus(room.getRoomStatus())
                .build();
    }
}
