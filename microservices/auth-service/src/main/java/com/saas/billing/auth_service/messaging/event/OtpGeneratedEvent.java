package com.saas.billing.auth_service.messaging.event;

public record OtpGeneratedEvent(
        String email,

        String otp,

        long expirationMinutes
) {}
