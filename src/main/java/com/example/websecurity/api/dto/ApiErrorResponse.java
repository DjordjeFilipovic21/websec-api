package com.example.websecurity.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class ApiErrorResponse {
    private ZonedDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private Long retryAfterSeconds;
    private ZonedDateTime lockoutUntil;
}
