package com.infotact.project1.dto.request;

import com.infotact.project1.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

/*
 * Request payload used for user registration.
 */

@Data
public class RegisterRequestDTO {

    private String firstName;

    private String lastName;

    private Gender gender;

    private String email;

    private String phone;

   // private LocalDate dateOfBirth;

    private String password;
}
/*
Backend automatically does:

user.setRole(Role.USER);

user.setAccountStatus(AccountStatus.ACTIVE);

and saves.
 */