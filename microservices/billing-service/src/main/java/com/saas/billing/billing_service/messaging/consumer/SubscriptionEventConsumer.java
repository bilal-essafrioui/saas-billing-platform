package com.saas.billing.billing_service.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.billing_service.messaging.KafkaTopics;
import com.saas.billing.billing_service.messaging.event.SubscriptionCreatedEvent;
import com.saas.billing.billing_service.service.BillingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionEventConsumer {

    private final ObjectMapper objectMapper;
    private final BillingService billingService;

    public SubscriptionEventConsumer(
            ObjectMapper objectMapper,
            BillingService billingService
    ) {
        this.objectMapper = objectMapper;
        this.billingService = billingService;
    }

    @KafkaListener(
            topics = KafkaTopics.SUBSCRIPTION_CREATED,
            groupId = "billing-service"
    )
    public void handleSubscriptionCreated(String message) {
        try {

            SubscriptionCreatedEvent event =
                    objectMapper.readValue(
                            message,
                            SubscriptionCreatedEvent.class
                    );

            billingService.billFirstSubscription(event);

        } catch (Exception e) {
            System.err.println(
                    "Error processing SubscriptionCreated : "
                            + e.getMessage()
            );
        }
    }
}