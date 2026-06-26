package com.infotact.project1.controller;

import com.infotact.project1.dto.request.BookingHoldRequestDTO;
import com.infotact.project1.dto.response.BookingHoldResponseDTO;
import com.infotact.project1.service.BookingHoldService;
import com.infotact.project1.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookinghold")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class BookingHoldController {

    // Coordinates complete booking workflow
    private final BookingService bookingService;

    // Handles booking hold persistence operations
    private final BookingHoldService bookingHoldService;

    // Create a temporary booking hold after acquiring lock and validating availability
    @PostMapping("/save")
    public BookingHoldResponseDTO createHold(
            @RequestBody BookingHoldRequestDTO requestDTO) {

        return bookingService.createBookingHold(
                requestDTO);
    }

    // Retrieve booking hold details using its unique hold id
    @GetMapping("/get/{holdId}")
    public BookingHoldResponseDTO getHoldById(
            @PathVariable String holdId) {

        return bookingHoldService.getHoldById(
                holdId);
    }

    // Cancels an active booking hold before payment completion
    @PatchMapping("/cancel/{holdId}")
    public String cancelHold(
            @PathVariable String holdId) {

        bookingHoldService.cancelHold(
                holdId);

        return "Booking Hold cancelled successfully";
    }
}