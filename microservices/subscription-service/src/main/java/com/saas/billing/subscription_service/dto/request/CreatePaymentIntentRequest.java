package com.saas.billing.subscription_service.dto.request;

import com.saas.billing.subscription_service.domain.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID planId;

    // null pour INITIAL_SUBSCRIPTION
    private UUID invoiceId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private String currency;

    @NotNull
    private PaymentType paymentType;

    // numéro de tentative (dunning)
    private int attemptNumber = 1;
}
