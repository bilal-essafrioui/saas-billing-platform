package com.saas.billing.dunning_service.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.dunning_service.messaging.KafkaTopics;
import com.saas.billing.dunning_service.messaging.event
        .SubscriptionSuspendedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DunningEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DunningEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSubscriptionSuspended(
            UUID subscriptionId,
            UUID userId,
            String userEmail) {

        SubscriptionSuspendedEvent event =
                new SubscriptionSuspendedEvent(
                        subscriptionId,
                        userId,
                        userEmail,
                        "Multiple payment failures after J+1, J+3, J+7"
                );

        publish(KafkaTopics.SUBSCRIPTION_SUSPENDED, event);
    }

    private void publish(String topic, Object event) {
        try {
            String message = objectMapper
                    .writeValueAsString(event);
            kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.err.println(
                                    "Failed to publish to "
                                            + topic + " : "
                                            + ex.getMessage()
                            );
                        }
                    });
        } catch (Exception e) {
            System.err.println(
                    "Error serializing event : "
                            + e.getMessage()
            );
        }
    }
}
