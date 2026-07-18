package com.saas.billing.subscription_service.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.subscription_service.domain.entity.Plan;
import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.dto.response.ProrataResponse;
import com.saas.billing.subscription_service.messaging.KafkaTopics;
import com.saas.billing.subscription_service.messaging.event.DowngradeScheduledEvent;
import com.saas.billing.subscription_service.messaging.event.PlanChangedEvent;
import com.saas.billing.subscription_service.messaging.event.SubscriptionCancelledEvent;
import com.saas.billing.subscription_service.messaging.event.SubscriptionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SubscriptionEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SubscriptionEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // PUBLIER SUBSCRIPTION CREATED
    // → billing-service écoute
    //   pour préparer la première facture
    // → notification-service écoute
    //   pour envoyer l'email de bienvenue
    // ════════════════════════════════════

    public void publishSubscriptionCreated(Subscription subscription, UUID paymentId) {
        SubscriptionCreatedEvent event = SubscriptionCreatedEvent.builder()
                .subscriptionId(subscription.getId())
                .paymentId(paymentId)
                .paymentId(paymentId)
                .userId(subscription.getUser().getUserId())
                .userEmail(subscription.getUser().getEmail())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getName())
                .planPrice(subscription.getPlan().getPrice())
                .startDate(subscription.getStartDate())
                .nextRenewalDate(subscription.getNextRenewalDate())
                .build();

        publish(KafkaTopics.SUBSCRIPTION_CREATED, event);
    }

    // ════════════════════════════════════
    // PUBLIER SUBSCRIPTION CANCELLED
    // → notification-service listens
    //   pour envoyer l'email de résiliation
    // ════════════════════════════════════

    public void publishSubscriptionCancelled(Subscription subscription) {
        SubscriptionCancelledEvent event = SubscriptionCancelledEvent
                .builder()
                .subscriptionId(subscription.getId())
                .userId(subscription.getUser().getUserId())
                .userEmail(subscription.getUser().getEmail())
                .planName(subscription.getPlan().getName())
                .cancelledAt(subscription.getCancelledAt())
                .build();

        publish(KafkaTopics.SUBSCRIPTION_CANCELLED, event);
    }

    // ════════════════════════════════════
    // PUBLIER PLAN CHANGED (UPGRADE)
    // → payment-service écoute
    //   pour déclencher le paiement prorata
    // → notification-service écoute
    //   pour envoyer l'email de confirmation
    // ════════════════════════════════════

    public void publishPlanChanged(
            Subscription subscription,
            ProrataResponse prorata) {

        PlanChangedEvent event = PlanChangedEvent.builder()
                .subscriptionId(subscription.getId())
                .userId(subscription.getUser().getUserId())
                .userEmail(subscription.getUser().getEmail())
                .previousPlanName(prorata.currentPlanName())
                .previousPlanPrice(prorata.currentPlanPrice())
                .newPlanName(prorata.newPlanName())
                .newPlanPrice(prorata.newPlanPrice())
                .changeType(prorata.changeType())
                .prorataAmount(prorata.prorataAmount())
                .effectiveDate(prorata.effectiveDate())
                .build();

        publish(KafkaTopics.PLAN_CHANGED, event);
    }

    // ════════════════════════════════════
    // PUBLIER DOWNGRADE SCHEDULED
    // → notification-service écoute
    //   pour informer le client du downgrade
    // ════════════════════════════════════

    public void publishDowngradeScheduled(
            Subscription subscription,
            Plan newPlan) {

        DowngradeScheduledEvent event = DowngradeScheduledEvent.builder()
                .subscriptionId(subscription.getId())
                .userId(subscription.getUser().getUserId())
                .userEmail(subscription.getUser().getEmail())
                .currentPlanName(subscription.getPlan().getName())
                .newPlanName(newPlan.getName())
                .newPlanPrice(newPlan.getPrice())
                .effectiveDate(subscription.getPendingPlanEffectiveDate())
                .build();

        publish(KafkaTopics.DOWNGRADE_SCHEDULED, event);
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE — SÉRIALISER ET PUBLIER
    // ════════════════════════════════════

    private void publish(String topic, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.err.println(
                                    "Failed to publish event to topic "
                                            + topic + " : " + ex.getMessage()
                            );
                        }
                    });

        } catch (Exception e) {
            System.err.println(
                    "Error serializing event for topic "
                            + topic + " : " + e.getMessage()
            );
        }
    }
}
