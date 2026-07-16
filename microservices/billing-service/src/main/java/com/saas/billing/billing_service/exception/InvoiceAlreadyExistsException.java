package com.saas.billing.billing_service.exception;

public class InvoiceAlreadyExistsException extends RuntimeException {
    public InvoiceAlreadyExistsException(String message) {
        super(message);
    }
}
