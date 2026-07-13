package com.saas.billing.subscription_service.messaging.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserCreatedEvent(
        UUID userId,
        String firstName,
        String lastName,
        String email
) {}
