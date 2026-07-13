package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.exception.SubscriptionNotFoundException;
import com.saas.billing.subscription_service.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class SubscriptionStatusUpdater {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionStateMachineService stateMachine;
    private final SubscriptionEventService eventService;

    public SubscriptionStatusUpdater(
            SubscriptionRepository subscriptionRepository,
            SubscriptionStateMachineService stateMachine,
            SubscriptionEventService eventService) {
        this.subscriptionRepository = subscriptionRepository;
        this.stateMachine = stateMachine;
        this.eventService = eventService;
    }

    // ════════════════════════════════════
    // PAIEMENT RÉUSSI
    // → PAST_DUE ou SUSPENDED → ACTIVE
    // → mettre à jour next_renewal_date
    // ════════════════════════════════════

    @Transactional
    public void onPaymentSucceeded(UUID subscriptionId) {
        Subscription subscription = getById(subscriptionId);
        SubscriptionStatus previous = subscription.getStatus();

        stateMachine.validateTransition(
                previous,
                SubscriptionStatus.ACTIVE
        );

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setNextRenewalDate(
                LocalDate.now().plusMonths(1)
        );
        subscriptionRepository.save(subscription);

        eventService.recordStatusChanged(
                subscription,
                previous,
                SubscriptionStatus.ACTIVE,
                "Payment succeeded"
        );
    }

    // ════════════════════════════════════
    // PAIEMENT ÉCHOUÉ
    // → ACTIVE → PAST_DUE
    // ════════════════════════════════════

    @Transactional
    public void onPaymentFailed(UUID subscriptionId) {
        Subscription subscription = getById(subscriptionId);
        SubscriptionStatus previous = subscription.getStatus();

        // seulement si ACTIVE
        // si déjà PAST_DUE → ignorer
        if (previous != SubscriptionStatus.ACTIVE) {
            return;
        }

        stateMachine.validateTransition(
                previous,
                SubscriptionStatus.PAST_DUE
        );

        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);

        eventService.recordStatusChanged(
                subscription,
                previous,
                SubscriptionStatus.PAST_DUE,
                "Payment failed"
        );
    }

    // ════════════════════════════════════
    // SUSPENSION
    // → PAST_DUE → SUSPENDED
    // appelé après J+7 sans succès
    // ════════════════════════════════════

    @Transactional
    public void onSubscriptionSuspended(UUID subscriptionId) {
        Subscription subscription = getById(subscriptionId);
        SubscriptionStatus previous = subscription.getStatus();

        stateMachine.validateTransition(
                previous,
                SubscriptionStatus.SUSPENDED
        );

        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscriptionRepository.save(subscription);

        eventService.recordStatusChanged(
                subscription,
                previous,
                SubscriptionStatus.SUSPENDED,
                "Suspended after multiple payment failures"
        );
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE
    // ════════════════════════════════════

    private Subscription getById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "Subscription not found : " + subscriptionId
                ));
    }
}