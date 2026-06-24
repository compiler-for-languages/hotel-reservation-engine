package com.infotact.project1.dto.request;

import lombok.Data;

import java.time.LocalDate;

/*
 * Request payload used to create a temporary booking hold.
 */

@Data
public class BookingHoldRequestDTO {

    private Long userId;

    private Long roomTypeId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;
}