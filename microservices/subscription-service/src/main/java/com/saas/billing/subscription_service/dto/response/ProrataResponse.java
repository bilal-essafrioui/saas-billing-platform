package com.saas.billing.subscription_service.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ProrataResponse(

        // Actual Plan
        UUID currentPlanId,
        String currentPlanName,
        BigDecimal currentPlanPrice,

        // new plan
        UUID newPlanId,
        String newPlanName,
        BigDecimal newPlanPrice,

        // calculate prorata
        int remainingDays,
        int totalDays,
        BigDecimal prorataAmount,

        // change type (event)
        String changeType, // "UPGRADE" ou "DOWNGRADE"

        // if downgrade → application date
        LocalDate effectiveDate,

        // if upgrade → pay right now
        // si downgrade → null (nothing to pay right now)
        BigDecimal amountToPayNow

) {}
