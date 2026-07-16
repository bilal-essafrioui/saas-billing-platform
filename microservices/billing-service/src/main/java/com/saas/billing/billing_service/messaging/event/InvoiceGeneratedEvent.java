package com.saas.billing.billing_service.messaging.event;

import com.saas.billing.billing_service.domain.enums.InvoiceType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record InvoiceGeneratedEvent(
        UUID invoiceId,
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        BigDecimal amount,
        InvoiceType type,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd
) {}
