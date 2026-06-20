package com.infotact.project1.dto.request;

import com.infotact.project1.enums.RoomStatus;
import lombok.Data;

/*
 * Request payload used to create a room.
 */

@Data
public class RoomRequestDTO {

    private String roomNumber;

    private Long roomTypeId;

    private Integer floorNumber;

    private RoomStatus roomStatus;
}