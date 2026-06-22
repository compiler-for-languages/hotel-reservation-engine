package com.infotact.project1.controller;

import com.infotact.project1.dto.request.ReservationPatchRequestDTO;
import com.infotact.project1.dto.request.ReservationRequestDTO;
import com.infotact.project1.dto.response.ReservationResponseDTO;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservation")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class ReservationController {

    // Dependency remains immutable after injection
    private final ReservationService reservationService;

    // Create a new reservation
    @PostMapping("/save")
    public ReservationResponseDTO createReservation(
            @RequestBody ReservationRequestDTO requestDTO) {

        return reservationService.createReservation(requestDTO);
    }

    // Retrieve all reservations
    @GetMapping("/getall")
    public List<ReservationResponseDTO> getAllReservations() {

        return reservationService.getAllReservations();
    }

    // Retrieve reservation by id
    @GetMapping("/get/{reservationId}")
    public ReservationResponseDTO getReservationById(
            @PathVariable Long reservationId) {

        return reservationService.getReservationById(reservationId);
    }

    // Retrieve reservations belonging to a user
    @GetMapping("/getbyuser")
    public List<ReservationResponseDTO> getReservationsByUser(
            @RequestParam Long userId) {

        return reservationService.getReservationsByUser(userId);
    }

    // Retrieve reservations by status
    @GetMapping("/getbystatus")
    public List<ReservationResponseDTO> getReservationsByStatus(
            @RequestParam ReservationStatus status) {

        return reservationService.getReservationsByStatus(status);
    }

    // Partially update reservation
    @PatchMapping("/update/{reservationId}")
    public ReservationResponseDTO updateReservation(
            @PathVariable Long reservationId,
            @RequestBody ReservationPatchRequestDTO requestDTO) {

        return reservationService.updateReservation(
                reservationId,
                requestDTO);
    }

    // Delete reservation by id
    @DeleteMapping("/delete/{reservationId}")
    public String deleteReservation(
            @PathVariable Long reservationId) {

        reservationService.deleteReservation(reservationId);

        return "Reservation deleted successfully";
    }
}