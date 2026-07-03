package com.infotact.project1.service;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.response.AvailabilityCustomerResponseDTO;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.enums.BookingHoldStatus;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.BookingHoldRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.StreamSupport;

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

    // Dependency remains immutable after injection
    private final BookingHoldRepository bookingHoldRepository;

    public AvailabilityResponseDTO checkAvailability(
            AvailabilityRequestDTO requestDTO) {

        RoomType roomType =
                roomTypeRepository.findById(
                                requestDTO.getRoomTypeId())
                        .orElseThrow(() ->
                                new RuntimeException("ROOM_TYPE_NOT_FOUND"));

        // Validate reservation dates
        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw new RuntimeException("INVALID_DATE_RANGE");
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

        // Count active booking holds overlapping requested dates
        long activeHolds = StreamSupport.stream(
                        bookingHoldRepository.findAll().spliterator(), false)
                .filter(Objects::nonNull)
                .filter(hold -> hold.getRoomTypeId().equals(roomType.getRoomTypeId()))
                .filter(hold -> hold.getStatus() == BookingHoldStatus.ACTIVE)
                .filter(hold ->
                        hold.getCheckInDate().isBefore(requestDTO.getCheckOutDate()) &&
                                hold.getCheckOutDate().isAfter(requestDTO.getCheckInDate()))
                .count();

        return mapToResponse(
                roomType,
                totalRooms,
                bookedRooms,
                activeHolds);
    }

    // Availability → DTO mapper
    private AvailabilityResponseDTO mapToResponse(
            RoomType roomType,
            long totalRooms,
            long bookedRooms,
            long activeHolds) {

        long availableRooms =
                Math.max(
                        0,
                        totalRooms
                                - bookedRooms
                                - activeHolds);

        boolean available =
                availableRooms > 0;

        // Builder pattern improves object creation readability
        return AvailabilityResponseDTO.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .roomTypeName(roomType.getName())
                .totalRooms(totalRooms)
                .bookedRooms(bookedRooms)
                .activeHolds(activeHolds)
                .availableRooms(availableRooms)
                .available(available)
                .build();
    }

    public  AvailabilityCustomerResponseDTO mapToCustomerResponse(
            AvailabilityResponseDTO response) {

        String message;
        RoomType roomType =
                roomTypeRepository.findById(response.getRoomTypeId()).orElseThrow(() ->
                new RuntimeException("ROOM_TYPE_NOT_FOUND"));

        if (!response.isAvailable()) {

            message = "Sold Out";

        } else if (response.getAvailableRooms() <= 3) {

            message = "Only "
                    + response.getAvailableRooms()
                    + " rooms left";

        } else {

            message = "Room Available";
        }

        return AvailabilityCustomerResponseDTO.builder()

                .roomTypeId(response.getRoomTypeId())

                .roomTypeName(response.getRoomTypeName())

                .availableRooms(response.getAvailableRooms())

                .available(response.isAvailable())

                // Fetch these from RoomType if present
                .capacity(roomType.getCapacity())
                .pricePerNight(roomType.getPricePerNight())

                .availabilityMessage(message)

                .build();
    }
}