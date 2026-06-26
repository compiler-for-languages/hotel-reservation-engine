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

import java.util.stream.StreamSupport;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class AvailabilityService {

    // Dependency remains immutable after injection
    // Repository to retrieve physical room inventory
    private final RoomRepository roomRepository;

    // Repository used to fetch room type details
    private final RoomTypeRepository roomTypeRepository;

    // Repository used to count confirmed reservations
    private final ReservationRepository reservationRepository;

    // Redis repository used to count temporary booking holds
    private final BookingHoldRepository bookingHoldRepository;

    // Calculate room availability for the requested room type and date range
    public AvailabilityResponseDTO checkAvailability(
            AvailabilityRequestDTO requestDTO) {

        // Validate that the requested roomtype exists
        RoomType roomType =
                roomTypeRepository.findById(
                                requestDTO.getRoomTypeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Room Type not found with id: "
                                                + requestDTO.getRoomTypeId()));

        // Ensure check-in date occurs before check-out date
        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw new RuntimeException(
                    "Check-in date must be before check-out date");
        }

        // Count total physical rooms belonging to the requested room type
        long totalRooms =
                roomRepository.countByRoomType(roomType);

        // Count reservations overlapping requested dates
        long bookedRooms =
                reservationRepository
                        .countOverlappingReservations(
                                requestDTO.getRoomTypeId(),
                                requestDTO.getCheckInDate(),
                                requestDTO.getCheckOutDate());

        // Count active Redis booking holds overlapping the requested dates
        long activeHolds =
                StreamSupport.stream(
                                bookingHoldRepository
                                        .findAll()
                                        .spliterator(),
                                false)

                        // Consider only holds for the requested room type
                        .filter(hold ->
                                hold.getRoomTypeId()
                                        .equals(roomType.getRoomTypeId()))

                        // Ignore expired or released booking holds
                        .filter(hold ->
                                hold.getStatus()
                                        == BookingHoldStatus.ACTIVE)

                        // Keep only holds whose dates overlap the requested stay
                        .filter(hold ->
                                hold.getCheckInDate()
                                        .isBefore(
                                                requestDTO.getCheckOutDate())
                                        &&
                                        hold.getCheckOutDate()
                                                .isAfter(
                                                        requestDTO.getCheckInDate()))
                        // Count remaining active holds
                        .count();

        // Convert calculated availability into response DTO
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

        // Prevent negative room availability
        long availableRooms =
                Math.max(
                        0,
                        totalRooms
                                - bookedRooms
                                - activeHolds);

        // Room type is available if at least one room remains
        boolean available =
                availableRooms > 0;

        // Builder pattern improves object creation readability
        return AvailabilityResponseDTO.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .roomTypeName(roomType.getName())
                .totalRooms(totalRooms)
                .bookedRooms(bookedRooms)
                .activeHolds(activeHolds)
                .availableRooms(availableRooms
                )
                .available(available)
                .build();
    }

    // Converts internal availability details into a customer-friendly response
    public  AvailabilityCustomerResponseDTO mapToCustomerResponse(
            AvailabilityResponseDTO response) {

        String message;

        // No rooms available
        if (!response.isAvailable()) {

            message = "Sold Out";

        } else if (response.getAvailableRooms() <= 3) { // Low inventory warning for customers

            message = "Only "
                    + response.getAvailableRooms()
                    + " rooms left";

        } else { // Sufficient rooms available

            message = "Available";
        }

        // Return only customer-visible information
        return AvailabilityCustomerResponseDTO.builder()

                .roomTypeId(response.getRoomTypeId())

                .roomTypeName(response.getRoomTypeName())

                .availableRooms(response.getAvailableRooms())

                .available(response.isAvailable())

                // Fetch these from RoomType if present
                //.capacity(...)
                //.pricePerNight(...)

                .availabilityMessage(message)

                .build();
    }
}