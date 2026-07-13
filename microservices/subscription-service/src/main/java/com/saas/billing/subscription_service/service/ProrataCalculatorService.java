package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.entity.Plan;
import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.dto.response.ProrataResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ProrataCalculatorService {

    // ════════════════════════════════════
    // CALCULATE PRORATA
    // called before upgrade/downgrade
    // to display the amount to the client
    // ════════════════════════════════════

    public ProrataResponse calculate(
            Subscription subscription,
            Plan newPlan) {

        Plan currentPlan = subscription.getPlan();
        LocalDate today = LocalDate.now();
        LocalDate startDate = subscription.getStartDate();
        LocalDate nextRenewalDate = subscription.getNextRenewalDate();

        // Remaining days in the current billing period
        int remainingDays = (int) ChronoUnit.DAYS.between(today, nextRenewalDate);

        // total days of the actual period
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, nextRenewalDate);

        // déterminer upgrade ou downgrade
        boolean isUpgrade = newPlan.getPrice()
                .compareTo(currentPlan.getPrice()) > 0;

        if (isUpgrade) {
            return calculateUpgradeProrata(
                    subscription, newPlan,
                    remainingDays, totalDays, today
            );
        } else {
            return calculateDowngradeProrata(
                    subscription, newPlan,
                    remainingDays, totalDays
            );
        }
    }

    // ════════════════════════════════════
    // CALCULATE PRORATA AMOUNT UPGRADE
    // formule :
    // (newPlanPrice - oldPlanPrice) × (remainingDays / totalDays)
    // ════════════════════════════════════

    private ProrataResponse calculateUpgradeProrata(
            Subscription subscription,
            Plan newPlan,
            int remainingDays,
            int totalDays,
            LocalDate today) {

        Plan currentPlan = subscription.getPlan();

        BigDecimal priceDiff = newPlan.getPrice()
                .subtract(currentPlan.getPrice());

        BigDecimal ratio = BigDecimal.valueOf(remainingDays)
                .divide(BigDecimal.valueOf(totalDays),
                        10, RoundingMode.HALF_UP);

        BigDecimal prorataAmount = priceDiff
                .multiply(ratio)
                .setScale(2, RoundingMode.HALF_UP);

        return ProrataResponse.builder()
                .currentPlanId(currentPlan.getId())
                .currentPlanName(currentPlan.getName())
                .currentPlanPrice(currentPlan.getPrice())
                .newPlanId(newPlan.getId())
                .newPlanName(newPlan.getName())
                .newPlanPrice(newPlan.getPrice())
                .remainingDays(remainingDays)
                .totalDays(totalDays)
                .prorataAmount(prorataAmount)
                .changeType("UPGRADE")
                .effectiveDate(today)
                .amountToPayNow(prorataAmount)
                .build();
    }

    // ════════════════════════════════════
    // CALCULATE PRORATA AMOUNT DOWNGRADE
    // no immediate payment
    // change at the next renouvellement
    // ════════════════════════════════════

    private ProrataResponse calculateDowngradeProrata(
            Subscription subscription,
            Plan newPlan,
            int remainingDays,
            int totalDays) {

        Plan currentPlan = subscription.getPlan();

        return ProrataResponse.builder()
                .currentPlanId(currentPlan.getId())
                .currentPlanName(currentPlan.getName())
                .currentPlanPrice(currentPlan.getPrice())
                .newPlanId(newPlan.getId())
                .newPlanName(newPlan.getName())
                .newPlanPrice(newPlan.getPrice())
                .remainingDays(remainingDays)
                .totalDays(totalDays)
                .prorataAmount(BigDecimal.ZERO)
                .changeType("DOWNGRADE")
                .effectiveDate(subscription.getNextRenewalDate())
                .amountToPayNow(null)
                .build();
    }
}
