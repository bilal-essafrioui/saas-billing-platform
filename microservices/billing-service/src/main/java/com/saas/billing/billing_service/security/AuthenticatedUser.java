package com.saas.billing.billing_service.security;

public record AuthenticatedUser(
        String userId,
        String email
) {}
