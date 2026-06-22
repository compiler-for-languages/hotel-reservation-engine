package com.infotact.project1.service;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class AvailabilityService {

    // Dependency remains immutable after injection
    private final RoomRepository roomRepository;

    // Dependency remains immutable after injection
    private final RoomTypeRepository roomTypeRepository;

    // Dependency remains immutable after injection
    private final ReservationRepository reservationRepository;

    public AvailabilityResponseDTO checkAvailability(
            AvailabilityRequestDTO requestDTO) {

        RoomType roomType =
                roomTypeRepository.findById(
                                requestDTO.getRoomTypeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Room Type not found with id: "
                                                + requestDTO.getRoomTypeId()));

        // Validate reservation dates
        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw new RuntimeException(
                    "Check-in date must be before check-out date");
        }

        // Total inventory available for this room type
        long totalRooms =
                roomRepository.countByRoomType(roomType);

        // Count reservations overlapping requested dates
        long bookedRooms =
                reservationRepository
                        .countOverlappingReservations(
                                requestDTO.getRoomTypeId(),
                                requestDTO.getCheckInDate(),
                                requestDTO.getCheckOutDate());

        return mapToResponse(
                roomType,
                totalRooms,
                bookedRooms);
    }

    // Availability → DTO mapper
    private AvailabilityResponseDTO mapToResponse(
            RoomType roomType,
            long totalRooms,
            long bookedRooms) {

        long availableRooms =
                totalRooms - bookedRooms;

        boolean available =
                availableRooms > 0;

        // Builder pattern improves object creation readability
        return AvailabilityResponseDTO.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .roomTypeName(roomType.getName())
                .totalRooms(totalRooms)
                .bookedRooms(bookedRooms)
                .availableRooms(availableRooms)
                .available(available)
                .build();
    }
}