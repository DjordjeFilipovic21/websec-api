package com.example.websecurity.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockUntil = new ConcurrentHashMap<>();

    private static final int[] LOCKOUT_SECONDS = {10, 30, 60, 300};

    private static final int MAX_ATTEMPTS = 3;

    public void recordFailure(String email) {
        int count = attempts.getOrDefault(email, 0) + 1;
        attempts.put(email, count);

        if (count >= MAX_ATTEMPTS) {
            int index = Math.min(count - MAX_ATTEMPTS, LOCKOUT_SECONDS.length - 1);
            int lockSeconds = LOCKOUT_SECONDS[index];
            lockUntil.put(email, Instant.now().plusSeconds(lockSeconds));
        }
    }
    public boolean isLocked(String email) {
        Instant until = lockUntil.get(email);
        if (until == null) return false;
        if (Instant.now().isAfter(until)) {
            lockUntil.remove(email);
            return false;
        }
        return true;
    }

    public long getSecondsUntilUnlock(String email) {
        Instant until = lockUntil.get(email);
        if (until == null) return 0;
        return Math.max(0, until.getEpochSecond() - Instant.now().getEpochSecond());
    }

    public void resetAttempts(String email) {
        attempts.remove(email);
        lockUntil.remove(email);
    }
}
