package com.saas.billing.payment_service.dto.response;

import lombok.Builder;

@Builder
public record CreateSetupIntentResponse(
        String clientSecret
) {
}
