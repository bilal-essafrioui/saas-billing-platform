package com.saas.billing.billing_service.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.billing_service.service.BillingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class SubscriptionDueConsumer {

    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    public SubscriptionDueConsumer(
            BillingService billingService,
            ObjectMapper objectMapper) {
        this.billingService = billingService;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // ÉCOUTER subscription-due
    // publié par subscription-service scheduler
    // chaque nuit pour les abonnements
    // dont next_renewal_date = aujourd'hui
    // ════════════════════════════════════

    @KafkaListener(
            topics = "subscription-due",
            groupId = "billing-service"
    )
    public void handleSubscriptionDue(String message) {
        try {
            var node = objectMapper.readTree(message);

            UUID subscriptionId = UUID.fromString(
                    node.get("subscriptionId").asText()
            );
            UUID userId = UUID.fromString(
                    node.get("userId").asText()
            );

            UUID planId = UUID.fromString(
                    node.get("planId").asText()
            );

            String userEmail = node.get("userEmail").asText();
            BigDecimal amount = new BigDecimal(
                    node.get("amount").asText()
            );
            LocalDate billingDate = LocalDate.parse(
                    node.get("billingDate").asText()
            );

            // créer la facture avec idempotence
            billingService.billSubscription(
                    subscriptionId,
                    userId,
                    userEmail,
                    planId,
                    amount,
                    billingDate
            );

            System.out.println(
                    "Invoice created for subscription : "
                            + subscriptionId
            );

        } catch (Exception e) {
            System.err.println(
                    "Error processing subscription-due : "
                            + e.getMessage()
            );
        }
    }

}