package com.infotact.project1.dto.request;

import lombok.Data;

@Data
public class ProfilePatchRequestDTO {

    private String firstName;

    private String lastName;

    private String phone;
}
