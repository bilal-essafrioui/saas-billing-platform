package com.saas.billing.payment_service.messaging;

public class KafkaTopics {

    // topics que payment-service ÉCOUTE
    public static final String FIRST_INVOICE_CREATED =
            "first-invoice-created";
    public static final String INVOICE_GENERATED =
            "invoice-generated";
    public static final String PRORATION_INVOICE_CREATED =
            "proration-invoice-created";

    // topics que payment-service PUBLIE
    public static final String PAYMENT_SUCCEEDED =
            "payment-succeeded";
    public static final String PAYMENT_FAILED =
            "payment-failed";

    public static final String UPGRADE_PAYMENT_SUCCEEDED =
            "upgrade-payment-succeeded";

    public static final String UPGRADE_PAYMENT_FAILED =
            "upgrade-payment-failed";
}