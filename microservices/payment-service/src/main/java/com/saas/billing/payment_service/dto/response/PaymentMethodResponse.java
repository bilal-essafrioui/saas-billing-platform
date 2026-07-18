package com.saas.billing.payment_service.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentMethodResponse(
        UUID id,
        UUID userId,
        String brand,
        String last4,
        Integer expMonth,
        Integer expYear,
        boolean isDefault,
        boolean active
) {}
