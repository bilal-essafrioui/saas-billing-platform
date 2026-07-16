package com.saas.billing.billing_service.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.billing_service.domain.entity.Invoice;
import com.saas.billing.billing_service.messaging.KafkaTopics;
import com.saas.billing.billing_service.messaging.event.InvoiceGeneratedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BillingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BillingEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // PUBLIER INVOICE GENERATED
    // → payment-service écoute
    //   pour déclencher le paiement Stripe
    // → notification-service écoute
    //   pour envoyer l'email "facture disponible"
    // ════════════════════════════════════

    public void publishInvoiceGenerated(Invoice invoice) {
        InvoiceGeneratedEvent event = InvoiceGeneratedEvent.builder()
                .invoiceId(invoice.getId())
                .subscriptionId(invoice.getSubscriptionId())
                .userId(invoice.getUserId())
                .userEmail(invoice.getUserEmail())
                .amount(invoice.getAmount())
                .type(invoice.getType())
                .billingPeriodStart(invoice.getBillingPeriodStart())
                .billingPeriodEnd(invoice.getBillingPeriodEnd())
                .build();

        publish(KafkaTopics.INVOICE_GENERATED, event);
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
