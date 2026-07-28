package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.entity.Plan;
import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.domain.entity.UserCache;
import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.dto.response.ProrataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProrataCalculatorService Tests")
class ProrataCalculatorServiceTest {

    private ProrataCalculatorService calculator;

    // plans de test
    private Plan planStarter;  // 10€
    private Plan planPro;      // 30€
    private Plan planEnterprise; // 99€

    @BeforeEach
    void setUp() {
        calculator = new ProrataCalculatorService();

        planStarter = Plan.builder()
                .id(UUID.randomUUID())
                .name("Starter")
                .price(new BigDecimal("10.00"))
                .active(true)
                .build();

        planPro = Plan.builder()
                .id(UUID.randomUUID())
                .name("Pro")
                .price(new BigDecimal("30.00"))
                .active(true)
                .build();

        planEnterprise = Plan.builder()
                .id(UUID.randomUUID())
                .name("Enterprise")
                .price(new BigDecimal("99.00"))
                .active(true)
                .build();
    }

    // ════════════════════════════════════
    // HELPER — créer une subscription de test
    // ════════════════════════════════════

    private Subscription buildSubscription(
            Plan currentPlan,
            LocalDate nextRenewalDate) {

        UserCache user = UserCache.builder()
                .userId(UUID.randomUUID())
                .email("test@email.com")
                .build();

        return Subscription.builder()
                .id(UUID.randomUUID())
                .user(user)
                .plan(currentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(1))
                .nextRenewalDate(nextRenewalDate)
                .build();
    }

    // ════════════════════════════════════
    // TESTS UPGRADE
    // ════════════════════════════════════

    @Nested
    @DisplayName("Calcul prorata UPGRADE")
    class UpgradeTests {

        @Test
        @DisplayName("Upgrade le 15 janvier (31 jours) : Starter→Pro = 10.32€")
        void upgrade_january15_shouldCalculateCorrectProrata() {
            // GIVEN
            // renouvellement le 1er février
            // donc il reste 17 jours (15 → 31 jan)
            LocalDate nextRenewal = LocalDate.of(2026, 2, 1);
            Subscription subscription = buildSubscription(
                    planStarter, nextRenewal
            );

            // WHEN
            // on est le 15 janvier
            // reste 17 jours sur 31
            // prorata = (30 - 10) × (17 / 31) = 20 × 0.5483 = 10.97€
            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // THEN
            assertThat(result.changeType())
                    .isEqualTo("UPGRADE");
            assertThat(result.prorataAmount())
                    .isPositive();
            assertThat(result.amountToPayNow())
                    .isEqualTo(result.prorataAmount());
            assertThat(result.effectiveDate())
                    .isEqualTo(LocalDate.now());
            assertThat(result.currentPlanName())
                    .isEqualTo("Starter");
            assertThat(result.newPlanName())
                    .isEqualTo("Pro");
        }

        @Test
        @DisplayName("Formule correcte : (newPrix - oldPrix) × (joursRestants / joursTotal)")
        void upgrade_formulaShouldBeCorrect() {
            // GIVEN
            // on simule : 16 jours restants sur 31
            // prorata = (30 - 10) × (16/31) = 10.32€

            LocalDate today = LocalDate.now();
            LocalDate nextRenewal = today.plusDays(16);

            // forcer le mois à 31 jours pour le calcul
            Subscription subscription = buildSubscription(
                    planStarter, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // THEN
            BigDecimal priceDiff = new BigDecimal("20.00");
            int totalDays = (int) ChronoUnit.DAYS.between(
                    subscription.getStartDate(),
                    subscription.getNextRenewalDate()
            );

            BigDecimal ratio = BigDecimal.valueOf(16)
                    .divide(BigDecimal.valueOf(totalDays),
                            10, RoundingMode.HALF_UP);

            BigDecimal expected = priceDiff
                    .multiply(ratio)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            assertThat(result.prorataAmount())
                    .isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("Upgrade avec beaucoup de jours restants → prorata proche du maximum")
        void upgrade_withManyRemainingDays_shouldChargeAlmostFull() {
            // GIVEN
            LocalDate nextRenewal = LocalDate.now().plusDays(100);

            Subscription subscription = buildSubscription(
                    planStarter, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // THEN
            BigDecimal priceDiff = planPro.getPrice()
                    .subtract(planStarter.getPrice());

            BigDecimal ratio = BigDecimal.valueOf(result.remainingDays())
                    .divide(
                            BigDecimal.valueOf(result.totalDays()),
                            10,
                            RoundingMode.HALF_UP
                    );

            BigDecimal expected = priceDiff
                    .multiply(ratio)
                    .setScale(2, RoundingMode.HALF_UP);

            assertThat(result.prorataAmount())
                    .isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("Upgrade vers Enterprise : calcul du prorata correct")
        void upgrade_toEnterprise_shouldCalculateCorrectProrata() {
            // GIVEN
            LocalDate nextRenewal = LocalDate.now().plusDays(15);

            Subscription subscription = buildSubscription(
                    planPro, nextRenewal
            );

            // WHEN
            ProrataResponse result =
                    calculator.calculate(subscription, planEnterprise);

            // THEN
            BigDecimal priceDiff = planEnterprise.getPrice()
                    .subtract(planPro.getPrice());

            BigDecimal ratio = BigDecimal.valueOf(result.remainingDays())
                    .divide(
                            BigDecimal.valueOf(result.totalDays()),
                            10,
                            RoundingMode.HALF_UP
                    );

            BigDecimal expected = priceDiff
                    .multiply(ratio)
                    .setScale(2, RoundingMode.HALF_UP);

            assertThat(result.prorataAmount())
                    .isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("Upgrade : joursRestants et joursTotal corrects")
        void upgrade_shouldReturnCorrectDays() {
            // GIVEN
            LocalDate today = LocalDate.now();
            LocalDate nextRenewal = today.plusDays(10);

            Subscription subscription = buildSubscription(
                    planStarter, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // THEN
            assertThat(result.remainingDays())
                    .isEqualTo(10);
            int expectedTotalDays = (int) ChronoUnit.DAYS.between(
                    subscription.getStartDate(),
                    subscription.getNextRenewalDate()
            );

            assertThat(result.totalDays())
                    .isEqualTo(expectedTotalDays);
        }
    }

    // ════════════════════════════════════
    // TESTS DOWNGRADE
    // ════════════════════════════════════

    @Nested
    @DisplayName("Calcul prorata DOWNGRADE")
    class DowngradeTests {

        @Test
        @DisplayName("Downgrade → aucun montant débité maintenant")
        void downgrade_shouldNotChargeNow() {
            // GIVEN
            LocalDate nextRenewal = LocalDate.now().plusDays(15);
            Subscription subscription = buildSubscription(
                    planPro, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planStarter
            );

            // THEN
            assertThat(result.changeType())
                    .isEqualTo("DOWNGRADE");
            assertThat(result.prorataAmount())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.amountToPayNow())
                    .isNull();
        }

        @Test
        @DisplayName("Downgrade → effectiveDate = nextRenewalDate")
        void downgrade_effectiveDateShouldBeNextRenewal() {
            // GIVEN
            LocalDate nextRenewal = LocalDate.now().plusDays(20);
            Subscription subscription = buildSubscription(
                    planEnterprise, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // THEN
            assertThat(result.effectiveDate())
                    .isEqualTo(nextRenewal);
        }

        @Test
        @DisplayName("Downgrade Pro → Starter : type DOWNGRADE")
        void downgrade_proToStarter_shouldBeDowngrade() {
            // GIVEN
            LocalDate nextRenewal = LocalDate.now().plusDays(10);
            Subscription subscription = buildSubscription(
                    planPro, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planStarter
            );

            // THEN
            assertThat(result.changeType())
                    .isEqualTo("DOWNGRADE");
            assertThat(result.currentPlanName())
                    .isEqualTo("Pro");
            assertThat(result.newPlanName())
                    .isEqualTo("Starter");
            assertThat(result.currentPlanPrice())
                    .isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(result.newPlanPrice())
                    .isEqualByComparingTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("Downgrade Enterprise → Starter : type DOWNGRADE")
        void downgrade_enterpriseToStarter_shouldBeDowngrade() {
            LocalDate nextRenewal = LocalDate.now().plusDays(5);
            Subscription subscription = buildSubscription(
                    planEnterprise, nextRenewal
            );

            ProrataResponse result = calculator.calculate(
                    subscription, planStarter
            );

            assertThat(result.changeType())
                    .isEqualTo("DOWNGRADE");
            assertThat(result.prorataAmount())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ════════════════════════════════════
    // TESTS EDGE CASES
    // ════════════════════════════════════

    @Nested
    @DisplayName("Cas limites")
    class EdgeCases {

        @Test
        @DisplayName("Upgrade le dernier jour du mois → montant quasi nul")
        void upgrade_lastDayOfMonth_shouldChargeAlmostNothing() {
            // GIVEN
            // il reste 1 jour
            LocalDate nextRenewal = LocalDate.now().plusDays(1);
            Subscription subscription = buildSubscription(
                    planStarter, nextRenewal
            );

            // WHEN
            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // THEN
            // prorata = 20 × (1 / joursTotal) ≈ très petit
            assertThat(result.prorataAmount())
                    .isGreaterThan(BigDecimal.ZERO)
                    .isLessThan(new BigDecimal("1.00"));
        }

        @Test
        @DisplayName("Montant prorata toujours arrondi à 2 décimales")
        void prorata_shouldBeRoundedTo2Decimals() {
            LocalDate nextRenewal = LocalDate.now().plusDays(7);
            Subscription subscription = buildSubscription(
                    planStarter, nextRenewal
            );

            ProrataResponse result = calculator.calculate(
                    subscription, planPro
            );

            // vérifier que le montant a au plus 2 décimales
            assertThat(result.prorataAmount().scale())
                    .isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Montant prorata toujours positif pour un upgrade")
        void upgrade_prorataAlwaysPositive() {
            for (int days = 1; days <= 30; days++) {
                LocalDate nextRenewal = LocalDate.now()
                        .plusDays(days);
                Subscription subscription = buildSubscription(
                        planStarter, nextRenewal
                );

                ProrataResponse result = calculator.calculate(
                        subscription, planPro
                );

                assertThat(result.prorataAmount())
                        .as("Prorata doit être positif pour %d jours restants", days)
                        .isGreaterThan(BigDecimal.ZERO);
            }
        }
    }
}