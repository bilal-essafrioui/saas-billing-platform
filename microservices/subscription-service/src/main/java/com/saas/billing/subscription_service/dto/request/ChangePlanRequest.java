package com.saas.billing.subscription_service.dto.request;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangePlanRequest(
        @NotNull(message = "New plan ID is required")
        UUID newPlanId
) {}
