package com.saas.billing.subscription_service.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreatePaymentIntentResponse(

        String clientSecret,

        UUID paymentId,

        String paymentIntentId
) {}
