package com.saas.billing.dunning_service.messaging.event;

import java.util.UUID;

public record SubscriptionSuspendedEvent(
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        String reason
) {}
