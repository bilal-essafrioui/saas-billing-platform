package com.saas.billing.auth_service.security;

public record AuthenticatedUser(
        String userId,
        String email
) {}
