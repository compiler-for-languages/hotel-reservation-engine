package com.infotact.project1.dto.request;

import lombok.Data;

import java.time.LocalDate;

/*
 * Request payload used to create a reservation.
 */

@Data
public class ReservationRequestDTO {

    private Long userId;

    private Long roomTypeId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer guestCount;

    private String specialRequest;

    private String paymentMethod; // Customer's selected payment method 
}