package com.saas.billing.payment_service.dto.response;


import lombok.Builder;

import java.util.UUID;

@Builder
public record CreatePaymentIntentResponse(
        // retourné au frontend via subscription-service
        String clientSecret,

        // stocké côté frontend pour confirmer après paiement
        UUID paymentId,

        // pour que le frontend sache quel PaymentIntent
        String paymentIntentId
) {}
