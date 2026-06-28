package com.infotact.project1.service;

import com.infotact.project1.dto.request.BookingHoldRequestDTO;
import com.infotact.project1.dto.response.BookingHoldResponseDTO;
import com.infotact.project1.enums.BookingHoldStatus;
import com.infotact.project1.model.BookingHold;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.BookingHoldRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class BookingHoldService {

    // Redis repository storing temporary booking holds
    private final BookingHoldRepository bookingHoldRepository;

    // Repository used to validate customer existence
    private final UserRepository userRepository;

    // Repository used to validate room type existence
    private final RoomTypeRepository roomTypeRepository;

    // Creates a temporary booking hold with a five-minute expiration
    public BookingHoldResponseDTO createHold(
            BookingHoldRequestDTO requestDTO) {

        // Validate customer
        User user = userRepository.findById(
                        requestDTO.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + requestDTO.getUserId()));

        // Validate room type
        RoomType roomType = roomTypeRepository.findById(
                        requestDTO.getRoomTypeId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Room Type not found with id: "
                                        + requestDTO.getRoomTypeId()));

        // Ensure valid booking dates
        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw new RuntimeException(
                    "Check-in date must be before check-out date");
        }

        // Create new booking hold
        BookingHold bookingHold = new BookingHold();

        // Generate globally unique hold identifier
        bookingHold.setHoldId(
                UUID.randomUUID().toString());

        // Store customer reference
        bookingHold.setUserId(
                user.getUserId());

        // Store room type reference
        bookingHold.setRoomTypeId(
                roomType.getRoomTypeId());

        bookingHold.setReservationId(
                requestDTO.getReservationId());

        // Store booking period
        bookingHold.setCheckInDate(
                requestDTO.getCheckInDate());

        bookingHold.setCheckOutDate(
                requestDTO.getCheckOutDate());

        // Newly created hold starts in ACTIVE state
        bookingHold.setStatus(
                BookingHoldStatus.ACTIVE);

        // Record creation timestamp
        bookingHold.setCreatedAt(
                LocalDateTime.now());

        // Automatically expire hold after five minutes
        bookingHold.setExpiresAt(
                LocalDateTime.now().plusMinutes(5));

        // Persist booking hold in Redis
        BookingHold savedHold =
                bookingHoldRepository.save(bookingHold);

        return mapToResponse(savedHold);
    }

    // Retrieves booking hold using its unique identifier
    public BookingHoldResponseDTO getHoldById(
            String holdId) {

        BookingHold bookingHold =
                bookingHoldRepository.findById(holdId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Booking Hold not found with id: "
                                                + holdId));

        return mapToResponse(bookingHold);
    }

    // Marks an active booking hold as cancelled
    public void cancelHold(
            String holdId) {

        BookingHold bookingHold =
                bookingHoldRepository.findById(holdId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Booking Hold not found with id: "
                                                + holdId));

        bookingHold.setStatus(
                BookingHoldStatus.CANCELLED);

        bookingHoldRepository.save(bookingHold);
    }

    // Release active booking hold after payment
    public void releaseActiveHold(
            Long userId,
            Long roomTypeId,
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        BookingHold bookingHold =
                StreamSupport.stream(
                                bookingHoldRepository.findAll()
                                        .spliterator(),
                                false)
                        .filter(hold -> hold.getUserId().equals(userId))
                        .filter(hold -> hold.getRoomTypeId().equals(roomTypeId))
                        .filter(hold -> hold.getCheckInDate().equals(checkInDate))
                        .filter(hold -> hold.getCheckOutDate().equals(checkOutDate))
                        .filter(hold -> hold.getStatus() == BookingHoldStatus.ACTIVE)
                        .findFirst()
                        .orElse(null);
        // If an active booking hold exists, release it
        if (bookingHold != null) {

            bookingHold.setStatus(
                    BookingHoldStatus.CANCELLED);

            bookingHoldRepository.save(
                    bookingHold);
        }
    }

    // Entity → DTO mapper
    private BookingHoldResponseDTO mapToResponse(
            BookingHold bookingHold) {

        // Builder pattern improves object creation readability
        return BookingHoldResponseDTO.builder()
                .holdId(bookingHold.getHoldId())
                .userId(bookingHold.getUserId())
                .roomTypeId(bookingHold.getRoomTypeId())
                .checkInDate(bookingHold.getCheckInDate())
                .checkOutDate(bookingHold.getCheckOutDate())
                .status(bookingHold.getStatus())
                .createdAt(bookingHold.getCreatedAt())
                .expiresAt(bookingHold.getExpiresAt())
                .build();
    }
}