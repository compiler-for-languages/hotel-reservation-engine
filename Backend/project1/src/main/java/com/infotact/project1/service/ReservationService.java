package com.infotact.project1.service;

import com.infotact.project1.dto.request.*;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.dto.response.ReservationResponseDTO;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Guest;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.model.User;

import com.infotact.project1.repository.GuestRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomAssignmentRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class ReservationService {

    //
    private final ReservationRepository reservationRepository;

    //
    private final UserRepository userRepository;

    //
    private final RoomTypeRepository roomTypeRepository;

    private final AvailabilityService availabilityService;

    private final LockService lockService;

    private final BookingHoldService bookingHoldService;

    private final PaymentService paymentService;

    private final RoomAssignmentRepository roomAssignmentRepository;

    private final GuestRepository guestRepository;

    @Transactional
    public ReservationResponseDTO createReservation(
            ReservationRequestDTO requestDTO) {

        // Fetch customer creating the reservation
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        // Fetch requested room type
        RoomType roomType = roomTypeRepository.findById(
                        requestDTO.getRoomTypeId())
                .orElseThrow(() ->
                        new RuntimeException("ROOM_TYPE_NOT_FOUND"));
//before proceeding we are checking just whether the room exists or not we also need to check whether that particular room type has
// rooms available to reserve or not
        // Check check-in/check-out validity
        if (!requestDTO.getCheckInDate()
                .isBefore(requestDTO.getCheckOutDate())) {

            throw new RuntimeException("INVALID_DATE_RANGE");
        }

        // Validate room occupancy capacity
        // Primary customer (may or may not ) + additional guests

        if (requestDTO.getGuestCount() > roomType.getCapacity()) {
            throw new RuntimeException("ROOM_CAPACITY_EXCEEDED");
        }

        // Acquire distributed lock to prevent concurrent bookings for the same room type
        String lockName =
                "roomType:" + roomType.getRoomTypeId();

        RLock lock =
                lockService.acquireLock(lockName);

        Reservation savedReservation = null;

        try {

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

                throw new RuntimeException("ROOM_UNAVAILABLE");
            }


            // Create reservation in PENDING state

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

           savedReservation =
                    reservationRepository.save(reservation);

            BookingHoldRequestDTO holdRequest =
                    new BookingHoldRequestDTO();

            holdRequest.setReservationId(
                    savedReservation.getReservationId());

            holdRequest.setUserId(
                    user.getUserId());

            holdRequest.setRoomTypeId(
                    roomType.getRoomTypeId());

            holdRequest.setCheckInDate(
                    requestDTO.getCheckInDate());

            holdRequest.setCheckOutDate(
                    requestDTO.getCheckOutDate());

            bookingHoldService.createHold(holdRequest);


            // Automatically create payment record
            PaymentRequestDTO paymentRequest =
                    new PaymentRequestDTO();

            paymentRequest.setReservationId(
                    savedReservation.getReservationId());

            // multiple payment methods
            paymentRequest.setPaymentMethod(
                    requestDTO.getPaymentMethod());

            paymentService.createPayment(
                    paymentRequest);

            // Return reservation details
            return mapToResponse(savedReservation);
        }
        catch(Exception exception){
            // Release booking hold if it was already created
            if(savedReservation != null){
                bookingHoldService.releaseActiveHold(
                        savedReservation.getReservationId());
            }

            throw exception;
        }
        finally {

            // Always release the distributed lock
            lockService.releaseLock(lock);
        }


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
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        return mapToResponse(reservation);
    }

    // Retrieve reservations belonging to a user
    public List<ReservationResponseDTO> getReservationsByUser(
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

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
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }

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
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        if (roomAssignmentRepository.existsByReservation(reservation)) {
            throw new RuntimeException("ROOM_ASSIGNMENT_EXISTS");
        }

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
                .guests(mapGuests(reservation))
                .build();
    }

    private List<GuestResponseDTO> mapGuests(Reservation reservation) {

        return guestRepository.findByReservation(reservation)
                .stream()
                .map(this::mapGuestToResponse)
                .toList();
    }

    private GuestResponseDTO mapGuestToResponse(Guest guest) {

        return GuestResponseDTO.builder()
                .guestId(guest.getGuestId())
                .reservationId(guest.getReservation().getReservationId())
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .phone(guest.getPhone())
                .gender(guest.getGender())
                .dateOfBirth(guest.getDateOfBirth())
                .build();
    }
}