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

    // Provides Redis distributed locking
    private final LockService lockService;

    // Calculates room availability
    private final AvailabilityService availabilityService;

    // Persists booking hold information
    private final BookingHoldService bookingHoldService;

    // Creates booking hold only after lock acquisition and availability validation
    public BookingHoldResponseDTO createBookingHold(
            BookingHoldRequestDTO requestDTO) {

        // Unique lock per room type prevents concurrent double booking
        String lockName =
                "roomType:"
                        + requestDTO.getRoomTypeId();

        // Acquire distributed redis lock

        RLock lock =
                lockService.acquireLock(lockName);

        try {
            // Convert booking request into availability request
            AvailabilityRequestDTO availabilityRequest =
                    new AvailabilityRequestDTO();

            availabilityRequest.setRoomTypeId(
                    requestDTO.getRoomTypeId());

            availabilityRequest.setCheckInDate(
                    requestDTO.getCheckInDate());

            availabilityRequest.setCheckOutDate(
                    requestDTO.getCheckOutDate());

            // Recalculate availability inside the critical section
            AvailabilityResponseDTO availabilityResponse =
                    availabilityService
                            .checkAvailability(
                                    availabilityRequest);

            // Reject booking if no rooms remain
            if (!availabilityResponse.isAvailable()) {

                throw new RuntimeException(
                        "No rooms available for selected room type");
            }

            // Create temporary booking hold in Redis
            return bookingHoldService
                    .createHold(requestDTO);

        } finally {
            // Always release the Redis lock
            lockService.releaseLock(lock);
        }
    }
}