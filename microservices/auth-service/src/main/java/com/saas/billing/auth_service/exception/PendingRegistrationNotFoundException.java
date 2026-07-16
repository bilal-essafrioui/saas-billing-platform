package com.saas.billing.auth_service.exception;

public class PendingRegistrationNotFoundException extends RuntimeException {
    public PendingRegistrationNotFoundException(String message) {
        super(message);
    }
}
