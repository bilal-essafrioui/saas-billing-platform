package com.saas.billing.auth_service.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final String PREFIX = "otp:";
    private static final long OTP_EXPIRATION_MINUTES = 10;

    private final RedisTemplate<String, String> redisTemplate;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    public void saveOtp(String email, String otp) {

        redisTemplate.opsForValue().set(
                PREFIX + email,
                otp,
                OTP_EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public boolean verifyOtp(String email, String otp) {

        String storedOtp = redisTemplate.opsForValue().get(PREFIX + email);

        return storedOtp != null && storedOtp.equals(otp);
    }

    public void deleteOtp(String email) {

        redisTemplate.delete(PREFIX + email);
    }

    public boolean exists(String email) {

        return redisTemplate.hasKey(PREFIX + email);
    }

    public String getOtp(String email) {

        return redisTemplate.opsForValue().get(PREFIX + email);
    }

}
