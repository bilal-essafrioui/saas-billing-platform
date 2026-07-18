package com.saas.billing.subscription_service.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
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
