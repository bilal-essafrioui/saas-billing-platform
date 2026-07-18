package com.saas.billing.payment_service.security;

public record AuthenticatedUser(
        String userId,
        String email
) {}
