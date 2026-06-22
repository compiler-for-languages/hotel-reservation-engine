package com.infotact.project1.dto.response;

import com.infotact.project1.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data

// Builder pattern improves DTO object creation readability
@Builder
public class ReservationResponseDTO {

    private Long reservationId;

    private String userName;

    private String roomTypeName;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer guestCount;

    private ReservationStatus reservationStatus;

    private LocalDateTime bookingTime;

    private String specialRequest;
}