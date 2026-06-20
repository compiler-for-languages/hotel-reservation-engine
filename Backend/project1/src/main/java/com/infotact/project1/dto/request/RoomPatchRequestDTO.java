package com.infotact.project1.dto.request;

import com.infotact.project1.enums.RoomStatus;
import lombok.Data;

/*
 * Used for partial room updates.
 */

@Data
public class RoomPatchRequestDTO {

    private String roomNumber;

    private RoomStatus roomStatus;
}
/*
roomNumber -> Can be corrected/renamed
roomStatus -> Changes frequently

floorNumber -> Physical property
roomTypeId -> Physical room category
 */