package com.example.websecurity.facade;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.exception.AccountLockedException;
import com.example.websecurity.exception.InvalidCredentialsException;
import com.example.websecurity.persistence.User;
import com.example.websecurity.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AuthenticationFacadeIntegrationTest {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void authenticateShouldRejectLockedUser() {
        User user = createUser("locked@user.com", "password123");
        user.setFailedLoginAttempts(5);
        user.setLockoutUntil(ZonedDateTime.now().plusMinutes(2));
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(user.getEmail())
                .password("password123")
                .build();

        assertThrows(AccountLockedException.class, () -> authenticationFacade.authenticate(request));
    }

    @Test
    void authenticateShouldLockAfterFifthFailedAttempt() {
        User user = createUser("maja@maja.com", "password");
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(user.getEmail())
                .password("wrong-password")
                .build();

        for (int i = 0; i < 5; i++) {
            assertThrows(InvalidCredentialsException.class, () -> authenticationFacade.authenticate(request));
        }

        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow();
        assertEquals(5, updatedUser.getFailedLoginAttempts());
        assertNotNull(updatedUser.getLockoutUntil());
    }

    @Test
    void authenticateShouldResetLockoutAndReturnTokenOnSuccess() {
        User user = createUser("pera@pera.com", "password123");
        user.setFailedLoginAttempts(3);
        user.setLockoutUntil(ZonedDateTime.now().minusMinutes(1));
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(user.getEmail())
                .password("password123")
                .build();

        AuthenticationResponse response = authenticationFacade.authenticate(request);

        assertNotNull(response.getAccessToken());
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow();
        assertEquals(0, updatedUser.getFailedLoginAttempts());
        assertNull(updatedUser.getLockoutUntil());
    }

    private User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setFailedLoginAttempts(0);
        return user;
    }
}
