package com.saas.billing.subscription_service.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record DowngradeScheduledEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        String currentPlanName,
        String newPlanName,
        BigDecimal newPlanPrice,
        LocalDate effectiveDate
) {}
