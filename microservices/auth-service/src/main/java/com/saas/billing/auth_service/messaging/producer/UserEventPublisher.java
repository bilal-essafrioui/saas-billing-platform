package com.saas.billing.auth_service.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.auth_service.domain.entity.User;
import com.saas.billing.auth_service.messaging.KafkaTopics;
import com.saas.billing.auth_service.messaging.event.UserCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    // PUBLIER USER CREATED
    // appelé après register()
    // → subscription-service écoute
    //   et stocke dans users_cache
    // ════════════════════════════════════

    public void publishUserCreated(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        publish(KafkaTopics.USER_CREATED, event);
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE
    // ════════════════════════════════════

    private void publish(String topic, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.err.println(
                                    "Failed to publish to topic "
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