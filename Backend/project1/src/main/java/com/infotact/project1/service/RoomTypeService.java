package com.infotact.project1.service;

import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.dto.response.RoomTypeResponseDTO;
import com.infotact.project1.exception.BusinessExceptions;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Autowired(required = false)
    private RoomRepository roomRepository;

    public RoomTypeResponseDTO createRoomType(RoomTypeRequestDTO requestDTO) {

        roomTypeRepository.findByName(requestDTO.getName())
                .ifPresent(room -> {
                    throw BusinessExceptions.roomTypeExists();
                });

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
        return roomTypeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoomTypeResponseDTO getRoomTypeById(Long roomTypeId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFound(roomTypeId));
        return mapToResponse(roomType);
    }

    public RoomTypeResponseDTO getRoomTypeByName(String name) {
        RoomType roomType = roomTypeRepository.findByName(name)
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFoundByName(name));
        return mapToResponse(roomType);
    }

    public void deleteRoomType(Long roomTypeId) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFound(roomTypeId));

        if (roomRepository != null && roomRepository.countByRoomType(roomType) > 0) {
            throw new RuntimeException(
                    "This room type cannot be deleted because rooms are currently assigned to it. Suggestion: Deactivate instead of deleting.");
        }

        roomTypeRepository.delete(roomType);
    }

    private RoomTypeResponseDTO mapToResponse(RoomType roomType) {
        return RoomTypeResponseDTO.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .pricePerNight(roomType.getPricePerNight())
                .capacity(roomType.getCapacity())
                .status(roomType.getStatus())
                .build();
    }

    public RoomTypeResponseDTO updateRoomType(Long roomTypeId, RoomTypeRequestDTO requestDTO) {

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFound(roomTypeId));

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
