package com.saas.billing.auth_service.service;

import com.saas.billing.auth_service.dto.request.PendingRegistrationRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PendingRegistrationService {

    private static final String PREFIX = "registration:";
    private static final long EXPIRATION_MINUTES = 10;

    private final RedisTemplate<String, PendingRegistrationRequest> redisTemplate;

    public PendingRegistrationService(
            @Qualifier("pendingRegistrationRedisTemplate")
            RedisTemplate<String, PendingRegistrationRequest> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public void save(PendingRegistrationRequest request) {

        redisTemplate.opsForValue().set(
                PREFIX + request.email(),
                request,
                EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public PendingRegistrationRequest get(String email) {

        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    public boolean exists(String email) {

        return redisTemplate.hasKey(PREFIX + email);
    }

    public void delete(String email) {

        redisTemplate.delete(PREFIX + email);
    }

}
