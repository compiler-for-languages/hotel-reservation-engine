package com.infotact.project1.service;

import com.infotact.project1.dto.request.GuestPatchRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.Guest;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.GuestRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class GuestService {

    // Dependency remains immutable after injection
    private final GuestRepository guestRepository;

    // Dependency remains immutable after injection
    private final ReservationRepository reservationRepository;

    private final UserRepository userRepository;

    public GuestResponseDTO createGuest(
            GuestRequestDTO requestDTO) {

        // Fetch reservation before creating guest
        Reservation reservation =
                reservationRepository.findById(
                                requestDTO.getReservationId())
                        .orElseThrow(() ->
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }
        authorizeGuestAccess(reservation);

        long enteredGuests =
                guestRepository.countByReservation(reservation);

        if (enteredGuests >= reservation.getGuestCount()) {

            throw new RuntimeException("GUEST_LIMIT_REACHED");
        }

        Guest guest = new Guest();

        guest.setReservation(reservation);
        guest.setFirstName(requestDTO.getFirstName());
        guest.setLastName(requestDTO.getLastName());
        guest.setPhone(
                requestDTO.getPhone() == null || requestDTO.getPhone().isBlank()
                        ? null
                        : requestDTO.getPhone());
        guest.setGender(requestDTO.getGender());
        guest.setDateOfBirth(requestDTO.getDateOfBirth());

        Guest savedGuest = guestRepository.save(guest);

        return mapToResponse(savedGuest);
    }

    public List<GuestResponseDTO> getAllGuests() {

        // Stream API for DTO conversion
        return guestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public GuestResponseDTO getGuestById(Long guestId) {

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() ->
                        new RuntimeException("GUEST_NOT_FOUND"));

        return mapToResponse(guest);
    }

    // Retrieve guests belonging to a reservation
    public List<GuestResponseDTO> getGuestsByReservation(
            Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("RESERVATION_NOT_FOUND"));

        authorizeGuestAccess(reservation);

        List<Guest> guests = guestRepository.findByReservation(reservation);

        return guests.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Partial guest update
    public GuestResponseDTO updateGuest(
            Long guestId,
            GuestPatchRequestDTO requestDTO) {

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() ->
                        new RuntimeException("GUEST_NOT_FOUND"));

        if (guest.getReservation().getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }

        authorizeGuestAccess(guest.getReservation());

        if (requestDTO.getFirstName() != null) {
            guest.setFirstName(requestDTO.getFirstName());
        }

        if (requestDTO.getLastName() != null) {
            guest.setLastName(requestDTO.getLastName());
        }

        if (requestDTO.getPhone() != null) {
            guest.setPhone(requestDTO.getPhone());
        }

        if (requestDTO.getGender() != null) {
            guest.setGender(requestDTO.getGender());
        }

        if (requestDTO.getDateOfBirth() != null) {
            guest.setDateOfBirth(requestDTO.getDateOfBirth());
        }

        Guest updatedGuest = guestRepository.save(guest);

        return mapToResponse(updatedGuest);
    }

    public void deleteGuest(Long guestId) {

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() ->
                        new RuntimeException("GUEST_NOT_FOUND"));

        if (guest.getReservation().getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }

        authorizeGuestAccess(guest.getReservation());

        guestRepository.delete(guest);
    }

    public void validateGuestDetailsCompleted(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        long enteredGuests =
                guestRepository.countByReservation(reservation);

        if (enteredGuests != reservation.getGuestCount()) {

            throw new RuntimeException("GUEST_DETAILS_INCOMPLETE");
        }
    }

    // Entity → DTO mapper
    private GuestResponseDTO mapToResponse(
            Guest guest) {

        // Builder pattern improves object creation readability
        return GuestResponseDTO.builder()
                .guestId(guest.getGuestId())
                .reservationId(
                        guest.getReservation().getReservationId())
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .phone(guest.getPhone())
                .gender(guest.getGender())
                .dateOfBirth(guest.getDateOfBirth())
                .build();
    }

    private void authorizeGuestAccess(Reservation reservation) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("AUTHENTICATION_REQUIRED");
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND"));

        Role role = currentUser.getRole();

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            return;
        }

        if (role == Role.CUSTOMER) {
            if (!reservation.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new RuntimeException("ACCESS_DENIED");
            }
            return;
        }

        throw new RuntimeException("ACCESS_DENIED");
    }
}