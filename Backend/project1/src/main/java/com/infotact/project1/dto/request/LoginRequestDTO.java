package com.infotact.project1.dto.request;

import lombok.Data;

/*
 * Request payload used for user login.
 */

@Data
public class LoginRequestDTO {

    private String email;

    private String password;
}