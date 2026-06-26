package com.infotact.project1.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * Represents all guests staying
 * under one reservation.
 */

@Data
@Builder
public class CurrentGuestResponseDTO {

    private Long reservationId;
    private String primaryCustomerName;
    private String roomNumber;

    private String roomType;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDateTime actualCheckIn;

    private List<GuestInfoResponseDTO> guests;
}