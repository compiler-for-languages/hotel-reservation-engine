package com.infotact.project1.dto.request;

import lombok.Data;

import java.time.LocalDate;

/*
 * Request payload used to check room availability.
 */

@Data
public class AvailabilityRequestDTO {

    private Long roomTypeId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;
}