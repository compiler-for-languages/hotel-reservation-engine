package com.infotact.project1.dto.response;

import lombok.Builder;
import lombok.Data;

@Data

// Builder pattern improves DTO object creation readability
@Builder
public class AvailabilityResponseDTO {

    private Long roomTypeId;

    private String roomTypeName;

    private Long totalRooms;

    private Long bookedRooms;

    private Long availableRooms;

    private boolean available;
}