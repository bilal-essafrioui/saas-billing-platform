package com.saas.billing.subscription_service.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record PlanChangedEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,

        // plan avant
        String previousPlanName,
        BigDecimal previousPlanPrice,

        // plan après
        String newPlanName,
        BigDecimal newPlanPrice,

        // UPGRADE ou DOWNGRADE
        String changeType,

        // si upgrade → montant débité
        BigDecimal prorataAmount,

        // si downgrade → date application
        LocalDate effectiveDate
) {}
