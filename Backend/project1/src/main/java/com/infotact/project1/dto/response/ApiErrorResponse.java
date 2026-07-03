package com.infotact.project1.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {

    private String timestamp;
    private int status;
    private String error;
    private String message;
}
