package com.infotact.project1.controller;

import com.infotact.project1.dto.request.AssignRoomRequestDTO;
import com.infotact.project1.dto.response.CurrentGuestResponseDTO;
import com.infotact.project1.dto.response.ReceptionDashboardResponseDTO;
import com.infotact.project1.dto.response.RoomAssignmentResponseDTO;
import com.infotact.project1.dto.response.TodayArrivalResponseDTO;
import com.infotact.project1.dto.response.TodayDepartureResponseDTO;
import com.infotact.project1.service.ReceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Handles receptionist operations.
 * Responsible for room assignment, guest check-in,
 * check-out and reception dashboard APIs.
 */

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
public class ReceptionController {

    // Business logic for receptionist operations
    private final ReceptionService receptionService;

    /*
     * Assign an available room to a confirmed reservation.
     */
    @PostMapping("/assign-room")
    public RoomAssignmentResponseDTO assignRoom(
            @Valid
            @RequestBody AssignRoomRequestDTO requestDTO) {

        return receptionService.assignRoom(requestDTO);
    }

    /*
     * Check in a guest.
     */
    @PatchMapping("/check-in")
    public RoomAssignmentResponseDTO checkIn(
            @Valid
            @RequestBody AssignRoomRequestDTO requestDTO) {

        return receptionService.checkIn(requestDTO);
    }

    /*
     * Check out a guest.
     */
    @PatchMapping("/check-out")
    public RoomAssignmentResponseDTO checkOut(
            @Valid
            @RequestBody AssignRoomRequestDTO requestDTO) {

        return receptionService.checkOut(requestDTO);
    }

    /*
     * View all confirmed arrivals scheduled for today.
     */
    @GetMapping("/today-arrivals")
    public List<TodayArrivalResponseDTO> getTodayArrivals() {

        return receptionService.getTodayArrivals();
    }

    /*
     * View all guests currently staying in the hotel.
     */
    @GetMapping("/current-guests")
    public List<CurrentGuestResponseDTO> getCurrentGuests() {

        return receptionService.getCurrentGuests();
    }

    /*
     * View all guests scheduled to check out today.
     */
    @GetMapping("/today-departures")
    public List<TodayDepartureResponseDTO> getTodayDepartures() {

        return receptionService.getTodayDepartures();
    }

    /*
     * View receptionist dashboard summary.
     */
    @GetMapping("/dashboard")
    public ReceptionDashboardResponseDTO getDashboard() {

        return receptionService.getDashboard();
    }
}