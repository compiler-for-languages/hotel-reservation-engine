package com.infotact.project1.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Represents a guest scheduled
 * to leave today.
 */

@Data
@Builder
public class TodayDepartureResponseDTO {

    private Long reservationId;

    private String customerName;

    private String roomNumber;

    private String roomType;

    private Integer guestCount;

    private String guestNames;

    private LocalDate checkOutDate;

    private LocalDateTime actualCheckIn;
}