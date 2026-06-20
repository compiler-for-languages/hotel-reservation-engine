package com.infotact.project1.dto.response;

import com.infotact.project1.enums.RoomStatus;
import lombok.Builder;
import lombok.Data;

/*
 * Response payload returned to clients.
 */

@Data

// Builder pattern improves DTO object creation readability
@Builder
public class RoomResponseDTO {

    private Long roomId;

    private String roomNumber;

    private String roomTypeName;

    private Integer floorNumber;

    private RoomStatus roomStatus;
}