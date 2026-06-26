package com.infotact.project1.dto.response;

import com.infotact.project1.enums.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Returned after successful room assignment.
 */

@Data
@Builder
public class RoomAssignmentResponseDTO {

    private Long assignmentId;

    private Long reservationId;

    private String customerName;

    private String roomNumber;

    private String roomType;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDateTime actualCheckIn;

    private LocalDateTime actualCheckOut;

    private LocalDateTime assignedAt;

    private AssignmentStatus assignmentStatus;
}