package com.example.websecurity.api;

import com.example.websecurity.api.dto.ApiErrorResponse;
import com.example.websecurity.exception.AccountLockedException;
import com.example.websecurity.exception.ForbiddenAccessException;
import com.example.websecurity.exception.InvalidCredentialsException;
import com.example.websecurity.exception.WebSecMissingDataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Duration;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebSecMissingDataException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingData(WebSecMissingDataException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), null, null);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenAccessException exception) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), null, null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), null, null);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountLocked(AccountLockedException exception) {
        ZonedDateTime now = ZonedDateTime.now();
        long retryAfterSeconds = Math.max(0L, Duration.between(now, exception.getLockoutUntil()).toSeconds());
        return build(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), retryAfterSeconds, exception.getLockoutUntil());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            Long retryAfterSeconds,
            ZonedDateTime lockoutUntil
    ) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(ZonedDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .retryAfterSeconds(retryAfterSeconds)
                .lockoutUntil(lockoutUntil)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
