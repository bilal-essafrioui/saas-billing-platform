package com.saas.billing.payment_service.dto.request;
import com.saas.billing.payment_service.domain.enums.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargePaymentRequest(

        UUID userId,

        String userEmail,

        UUID subscriptionId,

        UUID planId,

        BigDecimal amount,

        String currency,

        PaymentType paymentType

) {}
