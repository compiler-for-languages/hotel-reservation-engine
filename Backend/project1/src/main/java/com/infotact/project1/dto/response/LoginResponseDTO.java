package com.infotact.project1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * Response payload containing JWT token.
 */

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
}