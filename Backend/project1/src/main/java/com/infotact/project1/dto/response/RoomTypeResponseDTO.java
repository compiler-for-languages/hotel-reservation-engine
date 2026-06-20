package com.infotact.project1.dto.response;

import com.infotact.project1.enums.RoomTypeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/*
 * Response payload returned to clients.
 */

@Data
@Builder
// Builder pattern improves DTO object creation readability
public class RoomTypeResponseDTO {

    private Long roomTypeId;

    private String name;

    private String description;

    private BigDecimal pricePerNight;

    private Integer capacity;

    private RoomTypeStatus status;
}