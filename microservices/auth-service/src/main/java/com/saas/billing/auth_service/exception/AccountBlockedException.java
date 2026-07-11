package com.saas.billing.auth_service.exception;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String message) {
        super(message);
    }
}
