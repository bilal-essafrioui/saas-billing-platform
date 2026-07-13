package com.saas.billing.subscription_service.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PlanResponse(
        UUID id,
        String name,
        BigDecimal price,
        String description,
        boolean active
) {}
