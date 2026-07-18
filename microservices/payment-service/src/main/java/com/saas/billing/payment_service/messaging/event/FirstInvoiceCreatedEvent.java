package com.saas.billing.payment_service.messaging.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record FirstInvoiceCreatedEvent(
        UUID paymentId,
        UUID invoiceId,
        UUID subscriptionId,
        UUID userId
) {}
