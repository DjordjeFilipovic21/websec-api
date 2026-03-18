package com.example.websecurity.facade;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.security.JwtService;
import com.example.websecurity.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacade {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional
    public AuthenticationResponse authenticate(@NotNull AuthenticationRequest request) {
        log.info("Authentication Facade: Authenticating user with request: {}", request);
        var userOptional = userService.getOptionalUserByEmail(request.getEmail());

        if (userOptional.isPresent() && userService.isLoginBlocked(userOptional.get())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Login temporarily disabled. Try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            if (userOptional.isPresent()) {
                userService.registerFailedLogin(userOptional.get());
                if (userService.isLoginBlocked(userOptional.get())) {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed attempts. Login temporarily disabled.");
                }
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        } catch (LockedException ex) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Login temporarily disabled. Try again later.");
        }

        var user = userService.getUserByEmail(request.getEmail());
        userService.resetFailedLogins(user);
        var accessToken = jwtService.generateAccessToken(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
