package com.infotact.project1.service;

import com.infotact.project1.dto.request.*;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.dto.response.ReservationResponseDTO;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.exception.BusinessExceptions;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AvailabilityService availabilityService;
    private final LockService lockService;
    private final BookingHoldService bookingHoldService;
    private final PaymentService paymentService;

    @Autowired(required = false)
    private GuestRepository guestRepository;

    @Autowired(required = false)
    private RoomAssignmentRepository roomAssignmentRepository;

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO requestDTO) {

        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> BusinessExceptions.userNotFound(requestDTO.getUserId()));

        RoomType roomType = roomTypeRepository.findById(requestDTO.getRoomTypeId())
                .orElseThrow(() -> BusinessExceptions.roomTypeNotFound(requestDTO.getRoomTypeId()));

        if (!requestDTO.getCheckInDate().isBefore(requestDTO.getCheckOutDate())) {
            throw BusinessExceptions.invalidDateRange();
        }

        if (requestDTO.getGuestCount() > roomType.getCapacity()) {
            throw BusinessExceptions.roomCapacityExceeded();
        }

        String lockName = "roomType:" + roomType.getRoomTypeId();
        RLock lock = lockService.acquireLock(lockName);

        try {
            AvailabilityRequestDTO availabilityRequest = new AvailabilityRequestDTO();
            availabilityRequest.setRoomTypeId(roomType.getRoomTypeId());
            availabilityRequest.setCheckInDate(requestDTO.getCheckInDate());
            availabilityRequest.setCheckOutDate(requestDTO.getCheckOutDate());

            AvailabilityResponseDTO availability =
                    availabilityService.checkAvailability(availabilityRequest);

            if (!availability.isAvailable()) {
                throw BusinessExceptions.roomUnavailable(roomType.getName());
            }



            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setRoomType(roomType);
            reservation.setCheckInDate(requestDTO.getCheckInDate());
            reservation.setCheckOutDate(requestDTO.getCheckOutDate());
            reservation.setGuestCount(requestDTO.getGuestCount());
            reservation.setSpecialRequest(requestDTO.getSpecialRequest());
            reservation.setReservationStatus(ReservationStatus.PENDING);

            Reservation savedReservation = reservationRepository.save(reservation);

            // Create booking hold FIRST to temporarily reserve inventory
            BookingHoldRequestDTO holdRequest = new BookingHoldRequestDTO();
            holdRequest.setReservationId(reservation.getReservationId()); // Temporary ID, will be updated after reservation save
            holdRequest.setUserId(user.getUserId());
            holdRequest.setRoomTypeId(roomType.getRoomTypeId());
            holdRequest.setCheckInDate(requestDTO.getCheckInDate());
            holdRequest.setCheckOutDate(requestDTO.getCheckOutDate());

            bookingHoldService.createHold(holdRequest);

            // Mark hold as CONVERTED to prevent double counting
//            bookingHoldService.convertHoldToReservation(user.getUserId(), savedReservation.getReservationId());

            PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
            paymentRequest.setReservationId(savedReservation.getReservationId());
            paymentRequest.setPaymentMethod(requestDTO.getPaymentMethod());

            paymentService.createPayment(paymentRequest);

            return mapToResponse(savedReservation);
        } catch (Exception exception) {
            // If reservation was created but hold conversion failed, release the hold
            // This prevents orphaned holds from occupying inventory
            throw exception;
        } finally {
            lockService.releaseLock(lock);
        }
    }

    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservationResponseDTO getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(reservationId));
        return mapToResponse(reservation);
    }

    public List<ReservationResponseDTO> getReservationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessExceptions.userNotFound(userId));

        return reservationRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReservationResponseDTO> getReservationsByStatus(ReservationStatus reservationStatus) {
        return reservationRepository.findByReservationStatus(reservationStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReservationResponseDTO updateReservation(
            Long reservationId,
            ReservationPatchRequestDTO requestDTO) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(reservationId));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw BusinessExceptions.reservationAlreadyCheckedOut();
        }

        if (requestDTO.getReservationStatus() != null) {
            reservation.setReservationStatus(requestDTO.getReservationStatus());
        }

        if (requestDTO.getSpecialRequest() != null) {
            reservation.setSpecialRequest(requestDTO.getSpecialRequest());
        }

        if (requestDTO.getGuestCount() != null) {
            reservation.setGuestCount(requestDTO.getGuestCount());
        }

        Reservation updatedReservation = reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    public void deleteReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(reservationId));

        if (roomAssignmentRepository != null
                && roomAssignmentRepository.existsByReservation(reservation)) {
            throw BusinessExceptions.roomAssignmentExists();
        }

        reservationRepository.delete(reservation);
    }

    private ReservationResponseDTO mapToResponse(Reservation reservation) {
        return ReservationResponseDTO.builder()
                .reservationId(reservation.getReservationId())
                .userName(reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName())
                .roomTypeName(reservation.getRoomType().getName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestCount(reservation.getGuestCount())
                .reservationStatus(reservation.getReservationStatus())
                .bookingTime(reservation.getBookingTime())
                .specialRequest(reservation.getSpecialRequest())
                .guests(mapGuests(reservation))
                .build();
    }

    private List<GuestResponseDTO> mapGuests(Reservation reservation) {
        if (guestRepository == null) {
            return Collections.emptyList();
        }

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
