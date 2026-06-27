package com.infotact.project1.dto.request;

import com.infotact.project1.enums.ReservationStatus;
import lombok.Data;

/*
 * Used for partial reservation updates.
 */

@Data
public class ReservationPatchRequestDTO {

    private ReservationStatus reservationStatus;

    private String specialRequest;
    private Integer guestCount; // we can update guestCount also
}