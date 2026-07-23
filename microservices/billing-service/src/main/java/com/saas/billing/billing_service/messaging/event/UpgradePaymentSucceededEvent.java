package com.saas.billing.billing_service.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record UpgradePaymentSucceededEvent(

        UUID paymentId,

        UUID subscriptionId,

        UUID userId,

        String userEmail,

        UUID planId,

        BigDecimal amount,

        String currency

) {}
