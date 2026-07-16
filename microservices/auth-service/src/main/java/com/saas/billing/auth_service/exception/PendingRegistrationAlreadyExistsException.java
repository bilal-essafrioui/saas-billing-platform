package com.saas.billing.auth_service.exception;

public class PendingRegistrationAlreadyExistsException extends RuntimeException {
    public PendingRegistrationAlreadyExistsException(String message) {
        super(message);
    }
}
