package com.saas.billing.payment_service.messaging;

public class KafkaTopics {

    // topics que payment-service ÉCOUTE
    public static final String FIRST_INVOICE_CREATED =
            "first-invoice-created";
    public static final String INVOICE_GENERATED =
            "invoice-generated";

    // topics que payment-service PUBLIE
    public static final String PAYMENT_SUCCEEDED =
            "payment-succeeded";
    public static final String PAYMENT_FAILED =
            "payment-failed";
}