package com.saas.billing.payment_service.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID paymentId,
        UUID invoiceId,
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        UUID planId,
        BigDecimal amount,
        String currency
) {}