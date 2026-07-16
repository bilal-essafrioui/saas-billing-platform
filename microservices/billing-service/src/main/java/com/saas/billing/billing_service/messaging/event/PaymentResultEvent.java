package com.saas.billing.billing_service.messaging.event;

import java.util.UUID;

public record PaymentResultEvent(
        UUID invoiceId,
        UUID subscriptionId,
        UUID userId,
        String status  // "SUCCEEDED" ou "FAILED"
) {}