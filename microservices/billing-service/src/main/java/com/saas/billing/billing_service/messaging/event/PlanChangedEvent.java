package com.saas.billing.billing_service.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PlanChangedEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        String previousPlanName,
        BigDecimal previousPlanPrice,
        String newPlanNom,
        BigDecimal newPlanPrice,
        String changeType,
        BigDecimal prorataAmount,
        LocalDate effectiveDate
) {}