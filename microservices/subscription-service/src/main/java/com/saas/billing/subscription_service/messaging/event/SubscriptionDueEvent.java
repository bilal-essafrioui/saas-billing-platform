package com.saas.billing.subscription_service.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionDueEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        UUID planId,
        String planName,
        BigDecimal amount,
        LocalDate billingDate,
        LocalDate nextRenewalDate
) {}