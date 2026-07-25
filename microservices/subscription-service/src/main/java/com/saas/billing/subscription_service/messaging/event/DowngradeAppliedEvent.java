package com.saas.billing.subscription_service.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DowngradeAppliedEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        String previousPlanName,
        BigDecimal previousPlanPrice,
        String newPlanName,
        BigDecimal newPlanPrice,
        LocalDate appliedDate
) {}
