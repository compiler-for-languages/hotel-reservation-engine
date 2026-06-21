package com.infotact.project1.dto.request;

import com.infotact.project1.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

/*
 * Request payload used to create a guest.
 */

@Data
public class GuestRequestDTO {

    private Long reservationId;

    private String firstName;

    private String lastName;

    private String phone;

    private Gender gender;

    private LocalDate dateOfBirth;
}