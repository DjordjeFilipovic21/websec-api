package com.example.websecurity.service;

import com.example.websecurity.exception.WebSecMissingDataException;
import com.example.websecurity.persistence.User;
import com.example.websecurity.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    @Value("${websec.auth.max-failed-attempts:3}")
    private Integer maxFailedAttempts;
    @Value("${websec.auth.lock-duration-seconds:60}")
    private Integer lockDurationSeconds;

    public User getUserByEmail(String email) {
        log.info("User Service: Getting user from database by email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new WebSecMissingDataException("User with email " + email + " not found"));
        log.info("User Service: Found user with id: {} and email: {}", user.getId(), user.getEmail());
        return user;
    }

    public Optional<User> getOptionalUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isLoginBlocked(User user) {
        return user.getLoginBlockedUntil() != null && user.getLoginBlockedUntil().isAfter(ZonedDateTime.now());
    }

    @Transactional
    public void registerFailedLogin(User user) {
        int failedAttempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        failedAttempts++;
        user.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= maxFailedAttempts) {
            ZonedDateTime blockedUntil = ZonedDateTime.now().plusSeconds(lockDurationSeconds);
            user.setLoginBlockedUntil(blockedUntil);
            user.setFailedLoginAttempts(0);
            log.warn("User Service: User {} is blocked from login until {}", user.getEmail(), blockedUntil);
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetFailedLogins(User user) {
        if ((user.getFailedLoginAttempts() == null || user.getFailedLoginAttempts() == 0) && user.getLoginBlockedUntil() == null) {
            return;
        }

        user.setFailedLoginAttempts(0);
        user.setLoginBlockedUntil(null);
        userRepository.save(user);
    }
}
