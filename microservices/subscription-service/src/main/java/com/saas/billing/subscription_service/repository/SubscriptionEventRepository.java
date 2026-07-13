package com.saas.billing.subscription_service.repository;

import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.domain.entity.SubscriptionEvent;
import com.saas.billing.subscription_service.domain.enums.SubscriptionEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionEventRepository
        extends JpaRepository<SubscriptionEvent, UUID> {

    // ════════════════════════════════════
    // SUBSCRIPTION HISTORY
    // dashboard admin → CLIENT DETAILS
    // ════════════════════════════════════

    List<SubscriptionEvent> findBySubscriptionOrderByCreatedAtDesc(
            Subscription subscription
    );

    // ════════════════════════════════════
    // USER HISTORY
    // par userId directement
    // ════════════════════════════════════

    List<SubscriptionEvent> findByUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    // ════════════════════════════════════
    // FILTER BY EVENT TYPE
    // ex: ALL UPGRADES OF A USER
    // ════════════════════════════════════

    List<SubscriptionEvent> findBySubscriptionAndEventType(
            Subscription subscription,
            SubscriptionEventType eventType
    );
}
