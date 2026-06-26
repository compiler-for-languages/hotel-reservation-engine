package com.infotact.project1.dto.response;

import lombok.Builder;
import lombok.Data;

/*
 * Dashboard summary for receptionist.
 */

@Data
@Builder
public class ReceptionDashboardResponseDTO {

    private Long todayArrivals;

    private Long todayDepartures;

    private Long currentGuests;

    private Long availableRooms;

    private Long occupiedRooms;
}