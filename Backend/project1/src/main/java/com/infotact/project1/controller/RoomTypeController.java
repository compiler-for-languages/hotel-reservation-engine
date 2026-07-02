package com.infotact.project1.controller;

import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.dto.response.RoomTypeResponseDTO;
import com.infotact.project1.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roomtype")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    // Create a new room type
    @PostMapping("/save")
    public RoomTypeResponseDTO createRoomType(
            @RequestBody RoomTypeRequestDTO requestDTO) {

        return roomTypeService.createRoomType(requestDTO);
    }

    // Retrieve all room types
    @GetMapping("/getall")
    public List<RoomTypeResponseDTO> getAllRoomTypes() {

        return roomTypeService.getAllRoomTypes();
    }

    // Retrieve room type by id
    @GetMapping("/get/{roomTypeId}")
    public RoomTypeResponseDTO getRoomTypeById(

            @PathVariable Long roomTypeId) {

        return roomTypeService.getRoomTypeById(roomTypeId);

    }

    // Retrieve room type using request parameter name
    @GetMapping("/get")
    public RoomTypeResponseDTO getRoomTypeByName(
            @RequestParam String name) {

        return roomTypeService.getRoomTypeByName(name);
    }

    // Delete room type by id
    @DeleteMapping("/delete/{roomTypeId}")
    public String deleteRoomType(
            @PathVariable Long roomTypeId) {

        roomTypeService.deleteRoomType(roomTypeId);

        return "Room Type deleted successfully";
    }

    // Partially update room type
    @PatchMapping("/update/{roomTypeId}")
    public RoomTypeResponseDTO updateRoomType(
            @PathVariable Long roomTypeId,
            @RequestBody RoomTypeRequestDTO requestDTO) {

        return roomTypeService.updateRoomType(roomTypeId, requestDTO);
    }
}