package com.infotact.project1.dto.response;

import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data

// Builder pattern improves DTO object creation readability
@Builder
public class UserResponseDTO {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private Role role;

    private AccountStatus accountStatus;
}