package com.saas.billing.payment_service.dto.response;

import com.saas.billing.payment_service.domain.enums.PaymentStatus;

import java.util.UUID;

public record ChargePaymentResponse(

        UUID paymentId,

        PaymentStatus status,

        String message

) {}
