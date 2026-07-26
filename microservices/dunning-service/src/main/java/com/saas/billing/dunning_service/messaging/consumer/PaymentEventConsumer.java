package com.saas.billing.dunning_service.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.dunning_service.messaging.KafkaTopics;
import com.saas.billing.dunning_service.service.DunningService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final DunningService dunningService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(
            DunningService dunningService,
            ObjectMapper objectMapper) {
        this.dunningService = dunningService;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // PAYMENT FAILED
    // → planifier une relance
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "dunning-service"
    )
    public void handlePaymentFailed(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            UUID subscriptionId = UUID.fromString(
                    node.get("subscriptionId").asText()
            );
            UUID invoiceId = UUID.fromString(
                    node.get("invoiceId").asText()
            );
            UUID userId = UUID.fromString(
                    node.get("userId").asText()
            );
            String userEmail = node.has("userEmail")
                    && !node.get("userEmail").isNull()
                    ? node.get("userEmail").asText()
                    : "";

            BigDecimal amount = new BigDecimal(
                    node.get("amount").asText()
            );

            int attemptNumber = node.has("attemptNumber")
                    ? node.get("attemptNumber").asInt()
                    : 1;

            // planId peut ne pas être dans l'event
            // on le récupère depuis dunning_attempts
            // ou on le passe dans l'event payment-failed
            UUID planId = node.has("planId")
                    && !node.get("planId").isNull()
                    ? UUID.fromString(node.get("planId").asText())
                    : null;

            dunningService.scheduleRetry(
                    subscriptionId,
                    invoiceId,
                    userId,
                    userEmail,
                    planId,
                    amount,
                    "USD",
                    attemptNumber
            );

        } catch (Exception e) {
            System.err.println(
                    "Error processing payment-failed : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // PAYMENT SUCCEEDED
    // → annuler toutes les relances
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            groupId = "dunning-service"
    )
    public void handlePaymentSucceeded(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            UUID subscriptionId = UUID.fromString(
                    node.get("subscriptionId").asText()
            );

            // annuler toutes les relances planifiées
            dunningService.cancelRetries(subscriptionId);

        } catch (Exception e) {
            System.err.println(
                    "Error processing payment-succeeded : "
                            + e.getMessage()
            );
        }
    }
}
