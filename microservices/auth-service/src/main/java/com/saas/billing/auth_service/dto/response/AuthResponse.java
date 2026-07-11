package com.saas.billing.auth_service.dto.response;

import com.saas.billing.auth_service.domain.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Role role
) {}
