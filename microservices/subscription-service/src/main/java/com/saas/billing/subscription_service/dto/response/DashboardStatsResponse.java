package com.saas.billing.subscription_service.dto.response;

import lombok.Builder;

@Builder
public record DashboardStatsResponse(
        long activeCount,    // ACTIVE
        long pastDueCount,   // PAST_DUE
        long suspendedCount, // SUSPENDED
        long cancelledCount, // CANCELLED
        long totalCount     // all
) {}
