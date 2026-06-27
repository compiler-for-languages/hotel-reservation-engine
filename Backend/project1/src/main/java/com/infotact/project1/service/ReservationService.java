package com.infotact.project1.service;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.request.ReservationPatchRequestDTO;
import com.infotact.project1.dto.request.ReservationRequestDTO;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.dto.response.ReservationResponseDTO;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class ReservationService {

    // Dependency remains immutable after injection
    private final ReservationRepository reservationRepository;

    // Dependency remains immutable after injection
    private final UserRepository userRepository;

    // Dependency remains immutable after injection
    private final RoomTypeRepository roomTypeRepository;

    private final AvailabilityService availabilityService;

    public ReservationResponseDTO createReservation(
            ReservationRequestDTO requestDTO) {

        // Fetch customer creating the reservation
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + requestDTO.getUserId()));

        // Fetch requested room type
        RoomType roomType = roomTypeRepository.findById(
                        requestDTO.getRoomTypeId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Room Type not found with id: "
                                        + requestDTO.getRoomTypeId()));
//before proceeding we are checking just whether the room exists or not we also need to check whether that particular room type has
// rooms available to reserve or not
        // Check check-in/check-out validity
        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw new RuntimeException(
                    "Check-out date must be after check-in date");
        }

        // Validate room occupancy capacity


        if (requestDTO.getGuestCount() > roomType.getCapacity()) {
            throw new RuntimeException("Room capacity exceeded.");
        }
        //check room availability


        AvailabilityRequestDTO availabilityRequest =
                new AvailabilityRequestDTO();

        availabilityRequest.setRoomTypeId(
                roomType.getRoomTypeId());

        availabilityRequest.setCheckInDate(
                requestDTO.getCheckInDate());

        availabilityRequest.setCheckOutDate(
                requestDTO.getCheckOutDate());

        AvailabilityResponseDTO availability =
                availabilityService
                        .checkAvailability(
                                availabilityRequest);

        if (!availability.isAvailable()) {

            throw new RuntimeException(
                    "No rooms available for room type: "
                            + roomType.getName());
        }




        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setCheckInDate(requestDTO.getCheckInDate());
        reservation.setCheckOutDate(requestDTO.getCheckOutDate());
        reservation.setGuestCount(requestDTO.getGuestCount());
        reservation.setSpecialRequest(requestDTO.getSpecialRequest());

        // New reservations start in pending state
        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }

    public List<ReservationResponseDTO> getAllReservations() {

        // Stream API for DTO conversion
        return reservationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservationResponseDTO getReservationById(
            Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found with id: "
                                                + reservationId));

        return mapToResponse(reservation);
    }

    // Retrieve reservations belonging to a user
    public List<ReservationResponseDTO> getReservationsByUser(
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + userId));

        return reservationRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve reservations by status
    public List<ReservationResponseDTO> getReservationsByStatus(
            ReservationStatus reservationStatus) {

        return reservationRepository
                .findByReservationStatus(reservationStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Partial reservation update
    public ReservationResponseDTO updateReservation(
            Long reservationId,
            ReservationPatchRequestDTO requestDTO) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found with id: "
                                                + reservationId));

        if (requestDTO.getReservationStatus() != null) {
            reservation.setReservationStatus(
                    requestDTO.getReservationStatus());
        }

        if (requestDTO.getSpecialRequest() != null) {
            reservation.setSpecialRequest(
                    requestDTO.getSpecialRequest());
        }

        if (requestDTO.getGuestCount() != null) {
            reservation.setGuestCount(
                    requestDTO.getGuestCount());
        }




        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    public void deleteReservation(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found with id: "
                                                + reservationId));

        reservationRepository.delete(reservation);
    }

    // Entity → DTO mapper
    private ReservationResponseDTO mapToResponse(
            Reservation reservation) {

        // Builder pattern improves object creation readability
        return ReservationResponseDTO.builder()
                .reservationId(reservation.getReservationId())
                .userName(
                        reservation.getUser().getFirstName()
                                + " "
                                + reservation.getUser().getLastName())
                .roomTypeName(
                        reservation.getRoomType().getName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestCount(reservation.getGuestCount())
                .reservationStatus(
                        reservation.getReservationStatus())
                .bookingTime(reservation.getBookingTime())
                .specialRequest(
                        reservation.getSpecialRequest())
                .build();
    }
}