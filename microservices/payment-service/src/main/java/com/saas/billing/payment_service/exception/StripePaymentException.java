package com.saas.billing.payment_service.exception;

public class StripePaymentException extends RuntimeException {
    public StripePaymentException(String message) {
        super(message);
    }
}
