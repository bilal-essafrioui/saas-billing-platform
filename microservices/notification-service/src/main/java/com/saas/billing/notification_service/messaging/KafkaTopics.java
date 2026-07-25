package com.saas.billing.notification_service.messaging;

public class KafkaTopics {

    public static final String OTP_GENERATED =
            "otp-generated";
    public static final String SUBSCRIPTION_CREATED =
            "subscription-created";
    public static final String SUBSCRIPTION_CANCELLED =
            "subscription-cancelled";
    public static final String DOWNGRADE_SCHEDULED =
            "downgrade-scheduled";
    public static final String PLAN_CHANGED =
            "plan-changed";
    public static final String DOWNGRADE_APPLIED =
            "downgrade-applied";
    public static final String INVOICE_GENERATED =
            "invoice-generated";
    public static final String PAYMENT_SUCCEEDED =
            "payment-succeeded";
    public static final String PAYMENT_FAILED =
            "payment-failed";
    public static final String SUBSCRIPTION_SUSPENDED =
            "subscription-suspended";
}