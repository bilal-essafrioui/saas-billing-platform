package com.saas.billing.subscription_service.dto.response;

import com.saas.billing.subscription_service.domain.enums.SubscriptionEventType;
import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SubscriptionEventResponse(
        UUID id,
        SubscriptionEventType eventType,

        // plan avant
        String previousPlanName,
        BigDecimal previousPlanPrice,

        // plan après
        String newPlanName,
        BigDecimal newPlanPrice,

        // status avant/après
        SubscriptionStatus previousStatus,
        SubscriptionStatus newStatus,

        // prorata amount if it is an upgrade
        BigDecimal prorataAmount,

        String note,
        LocalDateTime createdAt
) {}
