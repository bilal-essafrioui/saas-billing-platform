package com.saas.billing.auth_service.dto.response;

import com.saas.billing.auth_service.domain.enums.Role;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {}
