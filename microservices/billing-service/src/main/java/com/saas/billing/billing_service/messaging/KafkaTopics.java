package com.saas.billing.billing_service.messaging;

public class KafkaTopics {
    // topics que billing-service ÉCOUTE
    public static final String SUBSCRIPTION_CREATED =
            "subscription-created";
    public static final String PLAN_CHANGED =
            "plan-changed";
    public static final String PAYMENT_SUCCEEDED =
            "payment-succeeded";
    public static final String PAYMENT_FAILED =
            "payment-failed";

    // topics que billing-service PUBLIE
    public static final String INVOICE_GENERATED =
            "invoice-generated";
    public static final String FIRST_INVOICE_CREATED =
            "first-invoice-created";

    public static final String PRORATION_INVOICE_CREATED =
            "proration-invoice-created";
}
