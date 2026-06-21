package com.infotact.project1.dto.response;

import com.infotact.project1.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data

// Builder pattern improves DTO object creation readability
@Builder
public class GuestResponseDTO {

    private Long guestId;

    private Long reservationId;

    private String firstName;

    private String lastName;

    private String phone;

    private Gender gender;

    private LocalDate dateOfBirth;
}