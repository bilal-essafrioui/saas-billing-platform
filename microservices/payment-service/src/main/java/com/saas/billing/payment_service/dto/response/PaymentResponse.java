package com.saas.billing.payment_service.dto.response;

import com.saas.billing.payment_service.domain.enums.PaymentStatus;
import com.saas.billing.payment_service.domain.enums.PaymentType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID id,
        UUID userId,
        UUID planId,
        UUID invoiceId,
        BigDecimal amount,
        String currency,
        PaymentType paymentType,
        PaymentStatus status,
        String failureReason,
        int attemptNumber,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {}
