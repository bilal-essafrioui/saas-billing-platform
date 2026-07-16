package com.saas.billing.auth_service.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BruteForceProtectionService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MINUTES = 15;
    private static final String ATTEMPTS_PREFIX = "login_attempts:";
    private static final String BLOCKED_PREFIX = "login_blocked:";

    private final RedisTemplate<String, String> redisTemplate;

    public BruteForceProtectionService( @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ════════════════════════════════════
    // VERIFY IF AN ACCOUNT IS BLOCKED
    // ════════════════════════════════════
    public boolean isBlocked(String email) {
        String key = BLOCKED_PREFIX + email;
        return redisTemplate.hasKey(key);
    }

    // ════════════════════════════════════
    // REGISTER A FAILED ATTEMPT
    // ════════════════════════════════════
    public void registerFailedAttempt(String email) {
        String attemptsKey = ATTEMPTS_PREFIX + email;

        // Increment counter
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        // Define expiration of 15 min if it is the first attempt
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(
                    attemptsKey,
                    BLOCK_DURATION_MINUTES,
                    TimeUnit.MINUTES
            );
        }

        // Block if too many attempts
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            blockAccount(email);
        }
    }

    // ════════════════════════════════════
    // RESET AFTER SUCCESS
    // ════════════════════════════════════
    public void resetAttempts(String email) {
        redisTemplate.delete(ATTEMPTS_PREFIX + email);
        redisTemplate.delete(BLOCKED_PREFIX + email);
    }

    // ════════════════════════════════════
    // GETTING REMAINING ATTEMPTS
    // ════════════════════════════════════
    public int getRemainingAttempts(String email) {
        String attemptsKey = ATTEMPTS_PREFIX + email;
        String value = redisTemplate.opsForValue().get(attemptsKey);
        if (value == null) {
            return isBlocked(email) ? 0 : MAX_ATTEMPTS;
        }
        int attempts = Integer.parseInt(value);
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    // ════════════════════════════════════
    // GET REMAINING BLOCK TIME
    // ════════════════════════════════════
    public long getBlockedMinutesRemaining(String email) {
        String key = BLOCKED_PREFIX + email;
        Long seconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (seconds == null || seconds <= 0) return 0;
        return (long) Math.ceil(seconds / 60.0);
    }

    // ════════════════════════════════════
    // PRIVATE METHOD
    // ════════════════════════════════════
    private void blockAccount(String email) {
        String blockedKey = BLOCKED_PREFIX + email;
        redisTemplate.opsForValue().set(
                blockedKey,
                "blocked",
                BLOCK_DURATION_MINUTES,
                TimeUnit.MINUTES
        );
        // DELETE COUNTER
        redisTemplate.delete(ATTEMPTS_PREFIX + email);
    }

}
