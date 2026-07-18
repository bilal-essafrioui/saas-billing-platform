package com.saas.billing.payment_service.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceGeneratedEvent(
        UUID invoiceId,
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        BigDecimal amount,
        String type,
        String billingPeriodStart,
        String billingPeriodEnd
) {}