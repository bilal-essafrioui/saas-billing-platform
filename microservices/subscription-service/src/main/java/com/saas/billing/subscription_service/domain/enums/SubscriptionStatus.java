package com.saas.billing.subscription_service.domain.enums;

public enum SubscriptionStatus {
    ACTIVE,     // Subscription is active (payment successful)
    PAST_DUE,   // Payment failed. Retry attempts scheduled for Day +1, Day +3, and Day +7.
    SUSPENDED,  // account suspended (payment failed after all attempts)
    CANCELLED   // Customer voluntarily canceled the subscription or it was permanently terminated.
}
