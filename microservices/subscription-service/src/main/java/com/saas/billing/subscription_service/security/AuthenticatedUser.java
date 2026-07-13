package com.saas.billing.subscription_service.security;

public record AuthenticatedUser(
        String userId,
        String email
) {}
