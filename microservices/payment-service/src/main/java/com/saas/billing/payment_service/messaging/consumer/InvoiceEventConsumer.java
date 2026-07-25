package com.saas.billing.payment_service.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.payment_service.messaging.KafkaTopics;
import com.saas.billing.payment_service.messaging.event.InvoiceGeneratedEvent;
import com.saas.billing.payment_service.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvoiceEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public InvoiceEventConsumer(
            PaymentService paymentService,
            ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // ÉCOUTER invoice-generated
    // publié par billing-service scheduler
    // → déclencher paiement automatique
    //   (pas besoin du frontend)
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.INVOICE_GENERATED,
            groupId = "payment-service"
    )
    public void handleInvoiceGenerated(String message) {
        try {
            InvoiceGeneratedEvent event = objectMapper
                    .readValue(message, InvoiceGeneratedEvent.class);

            // flux 2 — paiement automatique
            paymentService.processAutoPayment(
                    event.invoiceId(),
                    event.subscriptionId(),
                    event.userId(),
                    event.planId(), // planId pas dans l'event → à ajouter
                    event.userEmail(),
                    event.amount(),
                    "USD",
                    1 // premier essai
            );

        } catch (Exception e) {
            System.err.println(
                    "Error processing InvoiceGenerated : "
                            + e.getMessage()
            );
        }
    }
}
