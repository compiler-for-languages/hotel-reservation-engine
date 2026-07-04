package com.infotact.project1.service;

import com.infotact.project1.dto.request.GuestPatchRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.enums.Role;
import com.infotact.project1.exception.BusinessExceptions;
import com.infotact.project1.model.Guest;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.GuestRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public GuestResponseDTO createGuest(GuestRequestDTO requestDTO) {

        Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(requestDTO.getReservationId()));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw BusinessExceptions.reservationAlreadyCheckedOut();
        }
        authorizeGuestAccess(reservation);

        long enteredGuests = guestRepository.countByReservation(reservation);

        if (enteredGuests >= reservation.getGuestCount()) {
            throw BusinessExceptions.guestLimitReached();
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
        return guestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public GuestResponseDTO getGuestById(Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> BusinessExceptions.guestNotFound(guestId));
        return mapToResponse(guest);
    }

    public List<GuestResponseDTO> getGuestsByReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(BusinessExceptions::reservationNotFound);

        authorizeGuestAccess(reservation);

        return guestRepository.findByReservation(reservation)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public GuestResponseDTO updateGuest(Long guestId, GuestPatchRequestDTO requestDTO) {

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> BusinessExceptions.guestNotFound(guestId));

        if (guest.getReservation().getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw BusinessExceptions.reservationAlreadyCheckedOut();
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
                .orElseThrow(() -> BusinessExceptions.guestNotFound(guestId));

        if (guest.getReservation() != null
                && guest.getReservation().getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw BusinessExceptions.reservationAlreadyCheckedOut();
        }

        if (guest.getReservation() != null) {
            authorizeGuestAccess(guest.getReservation());
        }

        guestRepository.delete(guest);
    }

    public void validateGuestDetailsCompleted(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(reservationId));

        long enteredGuests = guestRepository.countByReservation(reservation);

        if (enteredGuests != reservation.getGuestCount()) {
            throw BusinessExceptions.guestDetailsIncomplete();
        }
    }

    private GuestResponseDTO mapToResponse(Guest guest) {
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

    private void authorizeGuestAccess(Reservation reservation) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(BusinessExceptions::userNotFound);

        Role role = currentUser.getRole();

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            return;
        }

        if (role == Role.CUSTOMER) {
            if (!reservation.getUser().getUserId().equals(currentUser.getUserId())) {
                throw BusinessExceptions.accessDenied();
            }
            return;
        }

        throw BusinessExceptions.accessDenied();
    }
}
