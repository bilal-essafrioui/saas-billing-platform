package com.saas.billing.billing_service.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.billing_service.messaging.KafkaTopics;
import com.saas.billing.billing_service.messaging.event.PaymentResultEvent;
import com.saas.billing.billing_service.messaging.event.PlanChangedEvent;
import com.saas.billing.billing_service.service.BillingService;
import com.saas.billing.billing_service.service.InvoiceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private final InvoiceService invoiceService;
    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(
            InvoiceService invoiceService,
            BillingService billingService,
            ObjectMapper objectMapper) {
        this.invoiceService = invoiceService;
        this.billingService = billingService;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // PAIEMENT RÉUSSI
    // → mettre à jour facture → PAID
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            groupId = "billing-service"
    )
    public void handlePaymentSucceeded(String message) {
        try {
            var node = objectMapper.readTree(message);
            UUID invoiceId = UUID.fromString(
                    node.get("invoiceId").asText()
            );
            invoiceService.markAsPaid(invoiceId);

        } catch (Exception e) {
            System.err.println(
                    "Error processing PaymentSucceeded : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // PAIEMENT ÉCHOUÉ
    // → mettre à jour facture → FAILED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "billing-service"
    )
    public void handlePaymentFailed(String message) {
        try {
            var node = objectMapper.readTree(message);
            UUID invoiceId = UUID.fromString(
                    node.get("invoiceId").asText()
            );
            invoiceService.markAsFailed(invoiceId);

        } catch (Exception e) {
            System.err.println(
                    "Error processing PaymentFailed : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // PLAN CHANGED (UPGRADE)
    // → générer facture de prorata
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PLAN_CHANGED,
            groupId = "billing-service"
    )
    public void handlePlanChanged(String message) {
        try {
            PlanChangedEvent event = objectMapper
                    .readValue(message, PlanChangedEvent.class);

            // seulement pour les upgrades
            if ("UPGRADE".equals(event.changeType())) {
                billingService.billUpgrade(event);
            }

        } catch (Exception e) {
            System.err.println(
                    "Error processing PlanChanged : "
                            + e.getMessage()
            );
        }
    }
}
