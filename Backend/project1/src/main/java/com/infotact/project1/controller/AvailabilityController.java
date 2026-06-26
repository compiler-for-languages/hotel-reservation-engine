package com.infotact.project1.controller;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.response.AvailabilityCustomerResponseDTO;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class AvailabilityController {

    // Dependency remains immutable after injection
    private final AvailabilityService availabilityService;

    // Check room availability for a room type
    @PostMapping("/check")
    public AvailabilityResponseDTO checkAvailability(
            @RequestBody AvailabilityRequestDTO requestDTO) {

        return availabilityService
                .checkAvailability(requestDTO);
    }

    @PostMapping("/search")
    public AvailabilityCustomerResponseDTO searchAvailability(
            @RequestBody AvailabilityRequestDTO requestDTO) {

        AvailabilityResponseDTO internalResponse =
                availabilityService.checkAvailability(requestDTO);
// Here I receive the object as normal availability response DTO and later, I map it to my AvailabilityCustomerResponseDTO
        return availabilityService
                .mapToCustomerResponse(internalResponse);
    }
}