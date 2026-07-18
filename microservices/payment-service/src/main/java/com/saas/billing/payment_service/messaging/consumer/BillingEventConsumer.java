package com.saas.billing.payment_service.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.payment_service.messaging.KafkaTopics;
import com.saas.billing.payment_service.messaging.event.FirstInvoiceCreatedEvent;
import com.saas.billing.payment_service.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BillingEventConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public BillingEventConsumer(
            ObjectMapper objectMapper,
            PaymentService paymentService
    ) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = KafkaTopics.FIRST_INVOICE_CREATED,
            groupId = "payment-service"
    )
    public void handleFirstInvoiceCreated(String message) {

        try {

            FirstInvoiceCreatedEvent event =
                    objectMapper.readValue(
                            message,
                            FirstInvoiceCreatedEvent.class
                    );

            paymentService.linkInvoiceAndSubscription(event);

        } catch (Exception e) {

            System.err.println(
                    "Error processing FirstInvoiceCreated : "
                            + e.getMessage()
            );

        }
    }
}
