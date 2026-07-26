package com.saas.billing.dunning_service.messaging;

public class KafkaTopics {

    // topics que dunning-service ÉCOUTE
    public static final String PAYMENT_FAILED =
            "payment-failed";
    public static final String PAYMENT_SUCCEEDED =
            "payment-succeeded";

    // topics que dunning-service PUBLIE
    public static final String SUBSCRIPTION_SUSPENDED =
            "subscription-suspended";
}