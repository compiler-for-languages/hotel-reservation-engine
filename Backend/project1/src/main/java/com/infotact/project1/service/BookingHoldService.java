package com.infotact.project1.service;

import com.infotact.project1.dto.request.BookingHoldRequestDTO;
import com.infotact.project1.dto.response.BookingHoldResponseDTO;
import com.infotact.project1.enums.BookingHoldStatus;
import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.*;
import com.infotact.project1.repository.*;
import com.infotact.project1.exception.BusinessExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Optional;
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

    private final PaymentRepository paymentRepository;
    private final ReservationRepository   reservationRepository;
    // Creates a temporary booking hold with a five-minute expiration
    public BookingHoldResponseDTO createHold(
            BookingHoldRequestDTO requestDTO) {

        // Validate customer
        User user = userRepository.findById(
                        requestDTO.getUserId())
                .orElseThrow(() ->
                        BusinessExceptions.userNotFound(requestDTO.getUserId()));

        RoomType roomType = roomTypeRepository.findById(
                        requestDTO.getRoomTypeId())
                .orElseThrow(() ->
                        BusinessExceptions.roomTypeNotFound(requestDTO.getRoomTypeId()));

        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw BusinessExceptions.invalidCheckInDateRange();
        }

        // Create new booking hold
        BookingHold bookingHold = new BookingHold();

        // Use reservation id as Redis key.
        // Makes it easy to identify the reservation when the key expires.
        bookingHold.setHoldId(
                requestDTO.getReservationId().toString());

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
                                BusinessExceptions.bookingHoldNotFound(holdId));

        return mapToResponse(bookingHold);
    }

    // Marks an active booking hold as cancelled
    public void cancelHold(
            String holdId) {

        BookingHold bookingHold =
                bookingHoldRepository.findById(holdId)
                        .orElseThrow(() ->
                                BusinessExceptions.bookingHoldNotFound(holdId));

        bookingHold.setStatus(
                BookingHoldStatus.CANCELLED);

        bookingHoldRepository.save(bookingHold);
    }

    // Release active booking hold after payment
    // Release active booking hold using reservation id
    //release hold if payment fails
    public void releaseActiveHold(Long reservationId) {

        // Remove booking hold from Redis
        bookingHoldRepository.deleteById(
                reservationId.toString());

        // Fetch payment
        Payment payment = paymentRepository
                .findByReservationReservationId(reservationId)
                .orElseThrow(() ->
                        BusinessExceptions.paymentNotFound(reservationId));

        // If payment failed, expire the reservation
        if (payment.getPaymentStatus() == PaymentStatus.FAILED) {

            Reservation reservation = reservationRepository
                    .findById(reservationId)
                    .orElseThrow(() ->
                            BusinessExceptions.reservationNotFound(reservationId));

            reservation.setReservationStatus(
                    ReservationStatus.EXPIRED);

            reservationRepository.save(reservation);
        }
    }









//    // Convert hold to reservation - mark as CONVERTED to prevent double counting
//    public void convertHoldToReservation(Long userId, Long reservationId) {
//        // Find the active hold for this user and update it
//        Iterable<BookingHold> allHolds = bookingHoldRepository.findAll();
//        for (BookingHold hold : allHolds) {
//            if (hold.getUserId().equals(userId) && hold.getStatus() == BookingHoldStatus.ACTIVE) {
//                hold.setHoldId(reservationId.toString()); // Update holdId to match reservationId for proper deletion
//                hold.setReservationId(reservationId);
//                hold.setStatus(BookingHoldStatus.CONVERTED);
//                bookingHoldRepository.save(hold);
//                break;
//            }
//        }
//    }

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
