package com.infotact.project1.controller;

import com.infotact.project1.dto.request.GuestPatchRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class GuestController {

    // Dependency remains immutable after injection
    private final GuestService guestService;

    // Create a new guest
    @PostMapping("/save")
    public GuestResponseDTO createGuest(
            @RequestBody GuestRequestDTO requestDTO) {

        return guestService.createGuest(requestDTO);
    }

    // Retrieve all guests
    @GetMapping("/getall")
    public List<GuestResponseDTO> getAllGuests() {

        return guestService.getAllGuests();
    }

    // Retrieve guest by id
    @GetMapping("/get/{guestId}")
    public GuestResponseDTO getGuestById(
            @PathVariable Long guestId) {

        return guestService.getGuestById(guestId);
    }

    // Retrieve guests belonging to a reservation
    @GetMapping("/getbyreservation")
    public List<GuestResponseDTO> getGuestsByReservation(
            @RequestParam Long reservationId) {

        return guestService.getGuestsByReservation(
                reservationId);
    }

    // Partially update guest
    @PatchMapping("/update/{guestId}")
    public GuestResponseDTO updateGuest(
            @PathVariable Long guestId,
            @RequestBody GuestPatchRequestDTO requestDTO) {

        return guestService.updateGuest(
                guestId,
                requestDTO);
    }

    // Delete guest by id
    @DeleteMapping("/delete/{guestId}")
    public String deleteGuest(
            @PathVariable Long guestId) {

        guestService.deleteGuest(guestId);

        return "Guest deleted successfully";
    }
}