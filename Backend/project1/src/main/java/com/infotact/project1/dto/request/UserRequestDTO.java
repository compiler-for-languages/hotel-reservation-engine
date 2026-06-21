package com.infotact.project1.dto.request;

import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import lombok.Data;

/*
 * Request payload used to create a user.
 */

@Data
public class UserRequestDTO {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String password;

    private Role role;
    
    private AccountStatus accountStatus;
}
