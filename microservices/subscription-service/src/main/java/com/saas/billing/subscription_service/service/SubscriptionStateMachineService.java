package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionStateMachineService {

    // ════════════════════════════════════
    // VALIDATE A TRANSITION
    // called before any status change (changement)
    // ════════════════════════════════════

    public void validateTransition(
            SubscriptionStatus current,
            SubscriptionStatus next) {

        boolean valid = switch (current) {
            case ACTIVE -> next == SubscriptionStatus.PAST_DUE
                    || next == SubscriptionStatus.CANCELLED;

            case PAST_DUE -> next == SubscriptionStatus.ACTIVE
                    || next == SubscriptionStatus.SUSPENDED
                    || next == SubscriptionStatus.CANCELLED;

            case SUSPENDED -> next == SubscriptionStatus.ACTIVE
                    || next == SubscriptionStatus.CANCELLED;

            case CANCELLED -> next == SubscriptionStatus.ACTIVE;
        };

        if (!valid) {
            throw new InvalidStateTransitionException(
                    "Transition invalide : "
                            + current + " → " + next
            );
        }
    }

    // ════════════════════════════════════
    // VERIFY IF SUBSCRIPTION ACCESSIBLE
    // ACTIVE + PAST_DUE = Authorized access
    // SUSPENDED + CANCELLED = blocked access
    // ════════════════════════════════════

    public boolean isAccessAllowed(SubscriptionStatus status) {
        return status == SubscriptionStatus.ACTIVE
                || status == SubscriptionStatus.PAST_DUE;
    }

    // ════════════════════════════════════
    // VERIFY IF change plan allowed
    // only if active
    // ════════════════════════════════════

    public boolean canChangePlan(SubscriptionStatus status) {
        return status == SubscriptionStatus.ACTIVE;
    }
}