package com.saas.billing.subscription_service.dto.request;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscribeRequest(
        @NotNull(message = "Plan ID is required")
        UUID planId,

        @NotNull(message = "Payment method ID is required")
        String stripePaymentMethodId
) {}
