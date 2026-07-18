package com.saas.billing.payment_service.exception;

public class PaymentAlreadyProcessedException extends RuntimeException {
    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }
}
