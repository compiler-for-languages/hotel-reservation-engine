package com.infotact.project1.dto.request;

import com.infotact.project1.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

/*
 * Used for partial guest updates.
 */

@Data
public class GuestPatchRequestDTO {

    private String firstName;

    private String lastName;

    private String phone;

    private Gender gender;

    private LocalDate dateOfBirth;
}