package com.saas.billing.subscription_service.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentSucceededEvent(
        UUID paymentId,
        UUID userId,
        String userEmail,
        UUID planId,
        BigDecimal amount,
        String currency
) {}
