package com.saas.billing.payment_service.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID paymentId,
        UUID invoiceId,
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        BigDecimal amount,
        String failureReason,
        int attemptNumber
) {}