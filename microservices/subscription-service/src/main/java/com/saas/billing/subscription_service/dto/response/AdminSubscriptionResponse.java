package com.saas.billing.subscription_service.dto.response;

import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AdminSubscriptionResponse(
        // vue admin enrichie
        // contient plus d'infos que SubscriptionResponse

        UUID subscriptionId,

        // user info
        UUID userId,
        String firstName,
        String lastName,
        String userEmail,

        // plan info
        String planName,
        BigDecimal planPrice,

        // subscription info
        SubscriptionStatus status,
        LocalDate startDate,
        LocalDate nextRenewalDate,
        LocalDateTime cancelledAt,

        // planified downgrade
        String pendingPlanName,
        LocalDate pendingPlanEffectiveDate
) {
}
