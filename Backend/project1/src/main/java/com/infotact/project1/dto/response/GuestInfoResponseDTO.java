package com.infotact.project1.dto.response;

import com.infotact.project1.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/*
 * Represents an individual guest currently
 * staying in the hotel.
 */

@Data
@Builder
public class GuestInfoResponseDTO {

    private Long guestId;

    private String firstName;

    private String lastName;

    private String phone;

    private Gender gender;

    private LocalDate dateOfBirth;
}