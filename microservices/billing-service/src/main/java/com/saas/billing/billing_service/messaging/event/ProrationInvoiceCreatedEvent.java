package com.saas.billing.billing_service.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ProrationInvoiceCreatedEvent(
        UUID invoiceId,

        UUID subscriptionId,

        UUID userId,

        String userEmail,

        BigDecimal amount

) {}
