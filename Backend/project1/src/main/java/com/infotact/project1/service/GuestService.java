package com.infotact.project1.service;

import com.infotact.project1.dto.request.GuestPatchRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.model.Guest;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.repository.GuestRepository;
import com.infotact.project1.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class GuestService {

    // Dependency remains immutable after injection
    private final GuestRepository guestRepository;

    // Dependency remains immutable after injection
    private final ReservationRepository reservationRepository;

    public GuestResponseDTO createGuest(
            GuestRequestDTO requestDTO) {

        // Fetch reservation before creating guest
        Reservation reservation =
                reservationRepository.findById(
                                requestDTO.getReservationId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found with id: "
                                                + requestDTO.getReservationId()));

        Guest guest = new Guest();

        guest.setReservation(reservation);
        guest.setFirstName(requestDTO.getFirstName());
        guest.setLastName(requestDTO.getLastName());
        guest.setPhone(requestDTO.getPhone());
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
                        new RuntimeException(
                                "Guest not found with id: " + guestId));

        return mapToResponse(guest);
    }

    // Retrieve guests belonging to a reservation
    public List<GuestResponseDTO> getGuestsByReservation(
            Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found with id: "
                                                + reservationId));

        return guestRepository.findByReservation(reservation)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Partial guest update
    public GuestResponseDTO updateGuest(
            Long guestId,
            GuestPatchRequestDTO requestDTO) {

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Guest not found with id: " + guestId));

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
                        new RuntimeException(
                                "Guest not found with id: " + guestId));

        guestRepository.delete(guest);
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
}