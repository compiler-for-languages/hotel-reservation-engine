package com.infotact.project1.dto.response;

import com.infotact.project1.enums.BookingHoldStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data

// Builder pattern improves DTO object creation readability
@Builder
public class BookingHoldResponseDTO {

    private String holdId;

    private Long userId;

    private Long roomTypeId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BookingHoldStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}