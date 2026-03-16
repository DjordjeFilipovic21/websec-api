package com.example.websecurity.facade;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.exception.AccountLockedException;
import com.example.websecurity.exception.InvalidCredentialsException;
import com.example.websecurity.persistence.User;
import com.example.websecurity.security.JwtService;
import com.example.websecurity.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacade {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 2;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})
    public AuthenticationResponse authenticate(@NotNull AuthenticationRequest request) {
        log.info("Authentication Facade: Authenticating user with email: {}", request.getEmail());

        Optional<User> foundUser = userService.findByEmail(request.getEmail());
        if (foundUser.isPresent()) {
            User existingUser = foundUser.get();
            if (isUserLocked(existingUser)) {
                throw new AccountLockedException("Too many failed login attempts. Try again later.", existingUser.getLockoutUntil());
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            foundUser.ifPresent(this::registerFailedAttempt);
            throw new InvalidCredentialsException("Invalid credentials");
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        var user = userService.getUserByEmail(request.getEmail());
        resetLoginAttempts(user);
        var accessToken = jwtService.generateAccessToken(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    private boolean isUserLocked(User user) {
        ZonedDateTime lockoutUntil = user.getLockoutUntil();
        return lockoutUntil != null && lockoutUntil.isAfter(ZonedDateTime.now());
    }

    private void registerFailedAttempt(User user) {
        int failedAttempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        failedAttempts++;
        user.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setLockoutUntil(ZonedDateTime.now().plusMinutes(LOCKOUT_MINUTES));
        }
        userService.save(user);
    }

    private void resetLoginAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        userService.save(user);
    }
}
