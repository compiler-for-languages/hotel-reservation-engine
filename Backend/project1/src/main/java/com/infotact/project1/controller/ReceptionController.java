package com.infotact.project1.controller;

import com.infotact.project1.dto.request.AssignRoomRequestDTO;
import com.infotact.project1.dto.response.*;
import com.infotact.project1.service.ReceptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reception")
@RequiredArgsConstructor
public class ReceptionController {

    private final ReceptionService receptionService;

    @PostMapping("/assign-room")
    public RoomAssignmentResponseDTO assignRoom(
            @Valid
            @RequestBody AssignRoomRequestDTO requestDTO) {

        return receptionService.assignRoom(requestDTO);
    }

    @PatchMapping("/check-in")
    public RoomAssignmentResponseDTO checkIn(
            @Valid
            @RequestBody AssignRoomRequestDTO requestDTO) {

        return receptionService.checkIn(requestDTO);
    }

    @PatchMapping("/check-out")
    public RoomAssignmentResponseDTO checkOut(
            @Valid
            @RequestBody AssignRoomRequestDTO requestDTO) {

        return receptionService.checkOut(requestDTO);
    }

    @GetMapping("/today-arrivals")
    public List<TodayArrivalResponseDTO> getTodayArrivals() {

        return receptionService.getTodayArrivals();
    }

    @GetMapping("/current-guests")
    public List<CurrentGuestResponseDTO> getCurrentGuests() {

        return receptionService.getCurrentGuests();
    }

    @GetMapping("/today-departures")
    public List<TodayDepartureResponseDTO> getTodayDepartures() {

        return receptionService.getTodayDepartures();
    }

    @GetMapping("/dashboard")
    public ReceptionDashboardResponseDTO getDashboard() {

        return receptionService.getDashboard();
    }
}
