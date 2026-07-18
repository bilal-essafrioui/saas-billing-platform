package com.saas.billing.subscription_service.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.subscription_service.domain.entity.UserCache;
import com.saas.billing.subscription_service.messaging.KafkaTopics;
import com.saas.billing.subscription_service.messaging.event.PaymentSucceededEvent;
import com.saas.billing.subscription_service.messaging.event.UserCreatedEvent;
import com.saas.billing.subscription_service.repository.UserCacheRepository;
import com.saas.billing.subscription_service.service.SubscriptionService;
import com.saas.billing.subscription_service.service.SubscriptionStatusUpdater;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private final UserCacheRepository userCacheRepository;
    private final ObjectMapper objectMapper;
    private final SubscriptionStatusUpdater subscriptionStatusUpdater;
    private final SubscriptionService subscriptionService;

    public UserEventConsumer(
            UserCacheRepository userCacheRepository,
            ObjectMapper objectMapper,
            SubscriptionStatusUpdater subscriptionStatusUpdater, SubscriptionService subscriptionService) {
        this.userCacheRepository = userCacheRepository;
        this.objectMapper = objectMapper;
        this.subscriptionStatusUpdater = subscriptionStatusUpdater;
        this.subscriptionService = subscriptionService;
    }

    // ════════════════════════════════════
    // ÉCOUTER user-created
    // publié par auth-service lors de l'inscription
    // → stocker user dans users_cache local
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.USER_CREATED,
            groupId = "subscription-service"
    )
    public void handleUserCreated(String message) {
        try {
            UserCreatedEvent event = objectMapper
                    .readValue(message, UserCreatedEvent.class);

            // idempotence → si user existe déjà, ignorer
            if (userCacheRepository.existsById(event.userId())) {
                return;
            }

            UserCache userCache = UserCache.builder()
                    .userId(event.userId())
                    .firstName(event.firstName())
                    .lastName(event.lastName())
                    .email(event.email())
                    .build();

            userCacheRepository.save(userCache);

        } catch (Exception e) {
            System.err.println(
                    "Error processing UserCreated event : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // ÉCOUTER payment-succeeded
    // → mettre à jour statut PAST_DUE → ACTIVE
    // → mettre à jour next_renewal_date
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            groupId = "subscription-service"
    )
    public void handlePaymentSucceeded(String message) {
        try {
            // désérialiser l'event
            /*var node = objectMapper.readTree(message);
            java.util.UUID subscriptionId = java.util.UUID.fromString(
                    node.get("subscriptionId").asText()
            );*/

            // déléguer au SubscriptionService
            // le service met à jour le statut
            // et la date de renouvellement
            //subscriptionStatusUpdater.onPaymentSucceeded(subscriptionId);

            PaymentSucceededEvent event =
                    objectMapper.readValue(message, PaymentSucceededEvent.class);

            subscriptionService.createSubscriptionAfterPayment(event);

        } catch (Exception e) {
            System.err.println(
                    "Error processing PaymentSucceeded event : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // ÉCOUTER payment-failed
    // → mettre à jour statut ACTIVE → PAST_DUE
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "subscription-service"
    )
    public void handlePaymentFailed(String message) {
        try {
            var node = objectMapper.readTree(message);
            java.util.UUID subscriptionId = java.util.UUID.fromString(
                    node.get("subscriptionId").asText()
            );

            subscriptionStatusUpdater.onPaymentFailed(subscriptionId);

        } catch (Exception e) {
            System.err.println(
                    "Error processing PaymentFailed event : "
                            + e.getMessage()
            );
        }
    }
}
