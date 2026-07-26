package com.saas.billing.dunning_service.dto.request;

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
public class RetryPaymentRequest {

    private UUID userId;
    private UUID planId;
    private UUID invoiceId;
    private BigDecimal amount;
    private String currency;
    private String paymentType;  // "RETRY"
    private int attemptNumber;
}
