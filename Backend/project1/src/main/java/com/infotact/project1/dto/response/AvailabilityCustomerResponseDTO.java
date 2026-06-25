package com.infotact.project1.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/*
 * Customer-facing availability response.
 *
 * Returned after the customer searches for rooms.
 * Contains only the information required to display
 * available room types and enable booking.
 */

@Data
@Builder
public class AvailabilityCustomerResponseDTO {

    // Unique identifier of the room type
    private Long roomTypeId;

    // Example: Deluxe Room, Standard Room, Suite
    private String roomTypeName;

    // Maximum guests allowed in this room type
    private Integer capacity;

    // Price charged per night
    private BigDecimal pricePerNight;

    // Number of rooms currently available
    private Long availableRooms;

    // Indicates whether booking is currently possible
    private boolean available;

    // Optional message for frontend display
    // Examples:
    // "Only 2 rooms left"
    // "Sold Out"
    // "Available"
    private String availabilityMessage;
}