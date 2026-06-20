package com.infotact.project1.dto.request;

import com.infotact.project1.enums.AccountStatus;
import lombok.Data;

/*
 * Used for partial user updates.
 */

@Data
public class UserPatchRequestDTO {

    private String firstName;

    private String lastName;

    private String phone;

    private AccountStatus accountStatus;
}
// Only these attributes can be modified
