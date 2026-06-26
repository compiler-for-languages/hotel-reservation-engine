package com.infotact.project1.dto.response;

import com.infotact.project1.enums.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/*
 * Represents a reservation arriving today.
 */

@Data
@Builder
public class TodayArrivalResponseDTO {

    private Long reservationId;

    private String customerName;

    private String phone;

    private String roomType;

    private Integer guestCount;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private boolean roomAssigned;

    private AssignmentStatus assignmentStatus;
}