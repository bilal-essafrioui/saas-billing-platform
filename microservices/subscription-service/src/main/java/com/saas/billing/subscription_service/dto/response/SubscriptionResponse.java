package com.saas.billing.subscription_service.dto.response;

import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SubscriptionResponse(
        UUID id,

        // user info
        UUID userId,
        String firstName,
        String lastName,
        String userEmail,

        // plan info
        UUID planId,
        String planName,
        BigDecimal planPrice,

        // subscription info
        SubscriptionStatus status,
        LocalDate startDate,
        LocalDate nextRenewalDate,
        LocalDateTime cancelledAt,

        // downgrade info
        // null si pas de downgrade planifié
        UUID pendingPlanId,
        String pendingPlanName,
        LocalDate pendingPlanEffectiveDate,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
