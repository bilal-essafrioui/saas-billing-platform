package com.saas.billing.billing_service.dto.response;

import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.InvoiceType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record InvoiceResponse(
        UUID id,
        UUID subscriptionId,
        UUID userId,
        String userEmail,
        BigDecimal amount,
        InvoiceStatus status,
        InvoiceType type,
        String idempotencyKey,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {}
