package com.infotact.project1.dto.request;

import com.infotact.project1.enums.RoomTypeStatus;
import lombok.Data;

import java.math.BigDecimal;

/*
 * sending from client to server used to create a room type.
 */

@Data
public class RoomTypeRequestDTO {

    private String name;

    private String description;

    private BigDecimal pricePerNight;

    private Integer capacity;

    private RoomTypeStatus status;
}
/*
RoomTypeRequestDto

Used when creating a room type.

name
description
pricePerNight
capacity
status

No:
roomTypeId
createdAt
updatedAt

because client shouldn't send them.
 */