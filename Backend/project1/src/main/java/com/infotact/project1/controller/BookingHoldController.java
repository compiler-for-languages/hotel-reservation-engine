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

    private final BookingService bookingService;

    private final BookingHoldService bookingHoldService;

    // Create a new booking hold
    @PostMapping("/save")
    public BookingHoldResponseDTO createHold(
            @RequestBody BookingHoldRequestDTO requestDTO) {

        return bookingService.createBookingHold(
                requestDTO);
    }

    // Retrieve booking hold by id
    @GetMapping("/get/{holdId}")
    public BookingHoldResponseDTO getHoldById(
            @PathVariable String holdId) {

        return bookingHoldService.getHoldById(
                holdId);
    }

    // Cancel booking hold
    @PatchMapping("/cancel/{holdId}")
    public String cancelHold(
            @PathVariable String holdId) {

        bookingHoldService.cancelHold(
                holdId);

        return "Booking Hold cancelled successfully";
    }
}