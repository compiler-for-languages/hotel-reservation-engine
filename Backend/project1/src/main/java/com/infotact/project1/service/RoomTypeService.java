package com.infotact.project1.service;

import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.dto.response.RoomTypeResponseDTO;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class RoomTypeService {
    //No necessity of autowired, The constructors are injected using import lombok.RequiredArgsConstructor;
    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeResponseDTO createRoomType(RoomTypeRequestDTO requestDTO) {


        RoomType roomType = new RoomType();

        roomType.setName(requestDTO.getName());
        roomType.setDescription(requestDTO.getDescription());
        roomType.setPricePerNight(requestDTO.getPricePerNight());
        roomType.setCapacity(requestDTO.getCapacity());
        roomType.setStatus(requestDTO.getStatus());

        RoomType savedRoomType = roomTypeRepository.save(roomType);

        return mapToResponse(savedRoomType);
    }

    public List<RoomTypeResponseDTO> getAllRoomTypes() {

        // Stream API for DTO conversion
        return roomTypeRepository.findAll()
                .stream()

                // Method reference improves readability
                .map(this::mapToResponse)
                .toList();
    }

    public RoomTypeResponseDTO getRoomTypeById(Long roomTypeId) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)

                // Prevents null object access
                .orElseThrow(() ->
                        new RuntimeException("Room Type not found with id: " + roomTypeId));

        return mapToResponse(roomType);
    }

    public RoomTypeResponseDTO getRoomTypeByName(String name) {

        RoomType roomType = roomTypeRepository.findByName(name)

                // Prevents null object access
                .orElseThrow(() ->
                        new RuntimeException("Room Type not found with name: " + name));

        return mapToResponse(roomType);
    }

    public void deleteRoomType(Long roomTypeId) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)

                // Prevents deletion of non-existent records
                .orElseThrow(() ->
                        new RuntimeException("Room Type not found with id: " + roomTypeId));

        roomTypeRepository.delete(roomType);
    }





    // Entity → DTO mapper
    private RoomTypeResponseDTO mapToResponse(RoomType roomType) {

        // Builder pattern improves object creation readability
        return RoomTypeResponseDTO.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .pricePerNight(roomType.getPricePerNight())
                .capacity(roomType.getCapacity())
                .status(roomType.getStatus())
                .build();
    }



    //update fields
    public RoomTypeResponseDTO updateRoomType(
            Long roomTypeId,
            RoomTypeRequestDTO requestDTO) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)

                // Prevents updates on non-existent records
                .orElseThrow(() ->
                        new RuntimeException("Room Type not found with id: " + roomTypeId));

        // Update only supplied fields

        if (requestDTO.getName() != null) {
            roomType.setName(requestDTO.getName());
        }

        if (requestDTO.getDescription() != null) {
            roomType.setDescription(requestDTO.getDescription());
        }

        if (requestDTO.getPricePerNight() != null) {
            roomType.setPricePerNight(requestDTO.getPricePerNight());
        }

        if (requestDTO.getCapacity() != null) {
            roomType.setCapacity(requestDTO.getCapacity());
        }

        if (requestDTO.getStatus() != null) {
            roomType.setStatus(requestDTO.getStatus());
        }

        RoomType updatedRoomType = roomTypeRepository.save(roomType);

        return mapToResponse(updatedRoomType);
    }
}