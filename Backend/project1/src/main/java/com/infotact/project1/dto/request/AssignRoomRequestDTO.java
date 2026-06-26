package com.infotact.project1.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/*
 * Request sent by the receptionist to assign
 * an available room to a confirmed reservation.
 */

@Data
public class AssignRoomRequestDTO {

    // Reservation selected by the receptionist
    @NotNull(message = "Reservation ID is required")
    private Long reservationId;
}