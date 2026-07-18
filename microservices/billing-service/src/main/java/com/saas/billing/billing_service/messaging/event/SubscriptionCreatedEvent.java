package com.saas.billing.billing_service.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionCreatedEvent(
        UUID subscriptionId,
        UUID paymentId,
        UUID userId,
        String userEmail,
        UUID planId,
        String planName,
        BigDecimal planPrice,
        LocalDate startDate,
        LocalDate nextRenewalDate
) {}