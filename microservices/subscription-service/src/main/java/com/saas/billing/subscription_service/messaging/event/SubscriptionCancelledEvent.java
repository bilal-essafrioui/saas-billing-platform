package com.saas.billing.subscription_service.messaging.event;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SubscriptionCancelledEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        String planName,
        LocalDateTime cancelledAt
) {}
