package com.saas.billing.billing_service.dto.request;


import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.PeriodValue;

public record FilterMyInvoicesRequest(
        InvoiceStatus status,

        PeriodValue period
) {}
