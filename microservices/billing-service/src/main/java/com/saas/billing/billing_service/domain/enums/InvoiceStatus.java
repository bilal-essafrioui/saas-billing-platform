package com.saas.billing.billing_service.domain.enums;

public enum InvoiceStatus {
    PENDING,  // bill created, payment not tried yet (pas encore tenté)
    PAID,     // payment succeed
    FAILED    // payment failed
}