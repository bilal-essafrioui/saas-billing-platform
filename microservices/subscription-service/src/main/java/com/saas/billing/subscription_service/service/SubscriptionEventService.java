package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.entity.Plan;
import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.domain.entity.SubscriptionEvent;
import com.saas.billing.subscription_service.domain.enums.SubscriptionEventType;
import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.dto.response.SubscriptionEventResponse;
import com.saas.billing.subscription_service.repository.SubscriptionEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionEventService {
    private final SubscriptionEventRepository eventRepository;

    public SubscriptionEventService(
            SubscriptionEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // ════════════════════════════════════
    // SAVE AN EVENT
    // called after every modification
    // ════════════════════════════════════

    @Transactional
    public void record(
            Subscription subscription,
            SubscriptionEventType eventType,
            Plan previousPlan,
            Plan newPlan,
            SubscriptionStatus previousStatus,
            SubscriptionStatus newStatus,
            BigDecimal prorataAmount,
            String note) {

        SubscriptionEvent event = SubscriptionEvent.builder()
                .subscription(subscription)
                .userId(subscription.getUser().getUserId())
                .eventType(eventType)
                .previousPlanId(previousPlan != null
                        ? previousPlan.getId() : null)
                .previousPlanName(previousPlan != null
                        ? previousPlan.getName() : null)
                .previousPlanPrice(previousPlan != null
                        ? previousPlan.getPrice() : null)
                .newPlanId(newPlan != null
                        ? newPlan.getId() : null)
                .newPlanName(newPlan != null
                        ? newPlan.getName() : null)
                .newPlanPrice(newPlan != null
                        ? newPlan.getPrice() : null)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .prorataAmount(prorataAmount)
                .note(note)
                .build();

        eventRepository.save(event);
    }

    // ════════════════════════════════════
    // RACCOURCIS FOR FREQUENT CASES
    // ════════════════════════════════════

    @Transactional
    public void recordSubscribed(Subscription subscription) {
        record(subscription,
                SubscriptionEventType.SUBSCRIBED,
                null,
                subscription.getPlan(),
                null,
                SubscriptionStatus.ACTIVE,
                null,
                "New subscription"
        );
    }

    @Transactional
    public void recordUpgraded(
            Subscription subscription,
            Plan previousPlan,
            BigDecimal prorataAmount
    ) {
        record(subscription,
                SubscriptionEventType.UPGRADED,
                previousPlan,
                subscription.getPlan(),
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.ACTIVE,
                prorataAmount,
                "Plan upgraded"
        );
    }

    @Transactional
    public void recordDowngradeScheduled(
            Subscription subscription,
            Plan newPlan
    ) {
        record(subscription,
                SubscriptionEventType.DOWNGRADE_SCHEDULED,
                subscription.getPlan(),
                newPlan,
                SubscriptionStatus.ACTIVE,
                SubscriptionStatus.ACTIVE,
                null,
                "Downgrade scheduled for " + subscription.getPendingPlanEffectiveDate()
        );
    }

    @Transactional
    public void recordStatusChanged(
            Subscription subscription,
            SubscriptionStatus previousStatus,
            SubscriptionStatus newStatus,
            String note) {
        record(subscription,
                SubscriptionEventType.STATUS_CHANGED,
                null,
                null,
                previousStatus,
                newStatus,
                null,
                note
        );
    }

    @Transactional
    public void recordCancelled(Subscription subscription) {
        record(subscription,
                SubscriptionEventType.CANCELLED,
                subscription.getPlan(),
                null,
                subscription.getStatus(),
                SubscriptionStatus.CANCELLED,
                null,
                "Subscription cancelled"
        );
    }

    @Transactional
    public void recordResubscribed(Subscription subscription) {
        record(subscription,
                SubscriptionEventType.RESUBSCRIBED,
                null,
                subscription.getPlan(),
                SubscriptionStatus.CANCELLED,
                SubscriptionStatus.ACTIVE,
                null,
                "Resubscribed after cancellation"
        );
    }

    // ════════════════════════════════════
    // GET HISTORY
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public List<SubscriptionEventResponse> getHistoryByUserId(UUID userId) {
        return eventRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionEventResponse> getHistoryBySubscription(
            Subscription subscription) {
        return eventRepository
                .findBySubscriptionOrderByCreatedAtDesc(subscription)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ════════════════════════════════════
    // CONVERSION ENTITY → DTO
    // ════════════════════════════════════

    private SubscriptionEventResponse toResponse(SubscriptionEvent event) {
        return SubscriptionEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .previousPlanName(event.getPreviousPlanName())
                .previousPlanPrice(event.getPreviousPlanPrice())
                .newPlanName(event.getNewPlanName())
                .newPlanPrice(event.getNewPlanPrice())
                .previousStatus(event.getPreviousStatus())
                .newStatus(event.getNewStatus())
                .prorataAmount(event.getProrataAmount())
                .note(event.getNote())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
