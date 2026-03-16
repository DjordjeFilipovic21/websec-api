package com.example.websecurity.exception;

import java.time.ZonedDateTime;

public class AccountLockedException extends RuntimeException {

    private final ZonedDateTime lockoutUntil;

    public AccountLockedException(String message, ZonedDateTime lockoutUntil) {
        super(message);
        this.lockoutUntil = lockoutUntil;
    }

    public ZonedDateTime getLockoutUntil() {
        return lockoutUntil;
    }
}
