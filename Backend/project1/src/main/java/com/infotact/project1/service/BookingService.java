package com.infotact.project1.service;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.request.BookingHoldRequestDTO;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.dto.response.BookingHoldResponseDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class BookingService {

    private final LockService lockService;

    private final AvailabilityService availabilityService;

    private final BookingHoldService bookingHoldService;

    public BookingHoldResponseDTO createBookingHold(
            BookingHoldRequestDTO requestDTO) {

        String lockName =
                "roomType:"
                        + requestDTO.getRoomTypeId();

        RLock lock =
                lockService.acquireLock(lockName);

        try {

            AvailabilityRequestDTO availabilityRequest =
                    new AvailabilityRequestDTO();

            availabilityRequest.setRoomTypeId(
                    requestDTO.getRoomTypeId());

            availabilityRequest.setCheckInDate(
                    requestDTO.getCheckInDate());

            availabilityRequest.setCheckOutDate(
                    requestDTO.getCheckOutDate());

            AvailabilityResponseDTO availabilityResponse =
                    availabilityService
                            .checkAvailability(
                                    availabilityRequest);

            if (!availabilityResponse.isAvailable()) {

                throw new RuntimeException(
                        "No rooms available for selected room type");
            }

            return bookingHoldService
                    .createHold(requestDTO);

        } finally {

            lockService.releaseLock(lock);
        }
    }
}