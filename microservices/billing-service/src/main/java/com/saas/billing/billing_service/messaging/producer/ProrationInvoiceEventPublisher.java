package com.saas.billing.billing_service.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.billing_service.messaging.KafkaTopics;
import com.saas.billing.billing_service.messaging.event.ProrationInvoiceCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProrationInvoiceEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProrationInvoiceEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishProrationInvoiceCreated(
            UUID invoiceId,
            UUID subscriptionId,
            UUID userId,
            String userEmail,
            BigDecimal amount
    ) {

        ProrationInvoiceCreatedEvent event =
                new ProrationInvoiceCreatedEvent(
                        invoiceId,
                        subscriptionId,
                        userId,
                        userEmail,
                        amount
                );

        publish(KafkaTopics.PRORATION_INVOICE_CREATED, event);
    }

    private void publish(String topic, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);

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