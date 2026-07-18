package com.saas.billing.payment_service.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.payment_service.messaging.KafkaTopics;
import com.saas.billing.payment_service.messaging.event.PaymentFailedEvent;
import com.saas.billing.payment_service.messaging.event.PaymentSucceededEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentSucceeded(
            UUID paymentId,
            UUID invoiceId,
            UUID subscriptionId,
            UUID userId,
            String userEmail,
            UUID planId,
            BigDecimal amount,
            String currency) {

        PaymentSucceededEvent event = new PaymentSucceededEvent(
                paymentId, invoiceId, subscriptionId,
                userId, userEmail, planId, amount, currency
        );
        publish(KafkaTopics.PAYMENT_SUCCEEDED, event);
    }

    public void publishPaymentFailed(
            UUID paymentId,
            UUID invoiceId,
            UUID subscriptionId,
            UUID userId,
            String userEmail,
            BigDecimal amount,
            String failureReason,
            int attemptNumber) {

        PaymentFailedEvent event = new PaymentFailedEvent(
                paymentId, invoiceId, subscriptionId,
                userId, userEmail, amount,
                failureReason, attemptNumber
        );
        publish(KafkaTopics.PAYMENT_FAILED, event);
    }

    private void publish(String topic, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.err.println(
                                    "Failed to publish to "
                                            + topic + " : " + ex.getMessage()
                            );
                        }
                    });
        } catch (Exception e) {
            System.err.println(
                    "Error serializing event : " + e.getMessage()
            );
        }
    }
}
