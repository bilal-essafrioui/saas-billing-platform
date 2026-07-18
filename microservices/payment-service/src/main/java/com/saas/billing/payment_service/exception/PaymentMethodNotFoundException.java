package com.saas.billing.payment_service.exception;

/**
 * @author $ {User}
 **/
public class PaymentMethodNotFoundException extends RuntimeException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
}
