package com.saas.billing.subscription_service.domain.enums;

public enum SubscriptionEventType {
    SUBSCRIBED,          // new subscription
    UPGRADED,            // Upgrade plan
    DOWNGRADE_SCHEDULED,
    DOWNGRADE_CANCELLED,// downgrade scheduled
    DOWNGRADE_APPLIED,   // downgrade applied
    STATUS_CHANGED,      // Status changed
    CANCELLED,           // résiliation
    RESUBSCRIBED         // re-subscription after annulation
}
