package com.saas.billing.subscription_service.messaging;

public class KafkaTopics {

    // topics que subscription-service listens
    public static final String USER_CREATED = "user-created";
    public static final String PAYMENT_SUCCEEDED = "payment-succeeded";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String SUBSCRIPTION_SUSPENDED =
            "subscription-suspended";

    // topics que subscription-service Publishes
    public static final String SUBSCRIPTION_CREATED = "subscription-created";
    public static final String SUBSCRIPTION_CANCELLED = "subscription-cancelled";
    public static final String DOWNGRADE_SCHEDULED = "downgrade-scheduled";
    public static final String PLAN_CHANGED = "plan-changed";
    public static final String SUBSCRIPTION_STATUS_UPDATED =
            "subscription-status-updated";

    // ════════════════════════════════════
    // NOUVEAUX TOPICS SCHEDULER
    // ════════════════════════════════════
    public static final String SUBSCRIPTION_DUE = "subscription-due";
    public static final String DOWNGRADE_APPLIED = "downgrade-applied";
}
