package com.infotact.project1.controller;

import com.infotact.project1.dto.request.RoomPatchRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.dto.response.RoomResponseDTO;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // Create a new room
    @PostMapping("/save")
    public RoomResponseDTO createRoom(
            @RequestBody RoomRequestDTO requestDTO) {

        return roomService.createRoom(requestDTO);
    }

    // Retrieve all rooms
    @GetMapping("/getall")
    public List<RoomResponseDTO> getAllRooms() {

        return roomService.getAllRooms();
    }

    // Retrieve room by id
    @GetMapping("/get/{roomId}")
    public RoomResponseDTO getRoomById(
            @PathVariable Long roomId) {

        return roomService.getRoomById(roomId);
    }

    // Retrieve room using room number
    @GetMapping("/get")
    public RoomResponseDTO getRoomByRoomNumber(
            @RequestParam String roomNumber) {

        return roomService.getRoomByRoomNumber(roomNumber);
    }

    // Retrieve all rooms belonging to a room type
    @GetMapping("/getbyroomtype")
    public List<RoomResponseDTO> getRoomsByRoomType(
            @RequestParam Long roomTypeId) {

        return roomService.getRoomsByRoomType(roomTypeId);
    }

    // Partially update room details
    @PatchMapping("/update/{roomId}")
    public RoomResponseDTO updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomPatchRequestDTO requestDTO) {

        return roomService.updateRoom(roomId, requestDTO);
    }

    // Retrieve rooms by room type and status
    @GetMapping("/filter")
    public List<RoomResponseDTO> getRoomsByRoomTypeAndStatus(
            @RequestParam Long roomTypeId,
            @RequestParam RoomStatus roomStatus) {

        return roomService.getRoomsByRoomTypeAndStatus(
                roomTypeId,
                roomStatus);
    }

    // Delete room by id
    @DeleteMapping("/delete/{roomId}")
    public String deleteRoom(
            @PathVariable Long roomId) {

        roomService.deleteRoom(roomId);

        return "Room deleted successfully";
    }
}