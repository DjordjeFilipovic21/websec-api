package com.example.websecurity.api;

import com.example.websecurity.api.dto.ApiErrorResponse;
import com.example.websecurity.exception.AccountLockedException;
import com.example.websecurity.exception.ForbiddenAccessException;
import com.example.websecurity.exception.InvalidCredentialsException;
import com.example.websecurity.exception.WebSecMissingDataException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapMissingDataTo404() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleMissingData(new WebSecMissingDataException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().getMessage());
    }

    @Test
    void shouldMapForbiddenTo403() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleForbidden(new ForbiddenAccessException("forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("forbidden", response.getBody().getMessage());
    }

    @Test
    void shouldMapInvalidCredentialsTo401() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void shouldMapAccountLockedTo429WithRetryInformation() {
        ZonedDateTime lockoutUntil = ZonedDateTime.now().plusSeconds(100);
        ResponseEntity<ApiErrorResponse> response =
                handler.handleAccountLocked(new AccountLockedException("Locked", lockoutUntil));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Locked", response.getBody().getMessage());
        assertNotNull(response.getBody().getRetryAfterSeconds());
        assertEquals(lockoutUntil, response.getBody().getLockoutUntil());
    }
}
