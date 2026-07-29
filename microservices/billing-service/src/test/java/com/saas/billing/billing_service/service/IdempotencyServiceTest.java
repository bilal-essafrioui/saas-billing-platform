package com.saas.billing.billing_service.service;

import com.saas.billing.billing_service.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyService Tests")
class IdempotencyServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    private UUID subscriptionId;

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
    }

    // ════════════════════════════════════
    // GÉNÉRATION DE CLÉ
    // ════════════════════════════════════

    @Nested
    @DisplayName("Génération de clé")
    class KeyGenerationTests {

        @Test
        @DisplayName("Clé RECURRING : subscriptionId + mois + année")
        void generateKey_shouldContainSubscriptionIdAndDate() {
            LocalDate date = LocalDate.of(2026, 8, 1);

            String key = idempotencyService.generateKey(
                    subscriptionId, date
            );

            assertThat(key)
                    .contains(subscriptionId.toString())
                    .contains("2026")
                    .contains("08");
        }

        @Test
        @DisplayName("Même mois → même clé")
        void generateKey_sameMonth_shouldProduceSameKey() {
            LocalDate date1 = LocalDate.of(2026, 8, 1);
            LocalDate date2 = LocalDate.of(2026, 8, 15);

            String key1 = idempotencyService.generateKey(
                    subscriptionId, date1
            );
            String key2 = idempotencyService.generateKey(
                    subscriptionId, date2
            );

            // même mois = même clé
            // une seule facture par mois par abonnement
            assertThat(key1).isEqualTo(key2);
        }

        @Test
        @DisplayName("Mois différents → clés différentes")
        void generateKey_differentMonths_shouldProduceDifferentKeys() {
            LocalDate august = LocalDate.of(2026, 8, 1);
            LocalDate september = LocalDate.of(2026, 9, 1);

            String keyAugust = idempotencyService.generateKey(
                    subscriptionId, august
            );
            String keySeptember = idempotencyService.generateKey(
                    subscriptionId, september
            );

            assertThat(keyAugust).isNotEqualTo(keySeptember);
        }

        @Test
        @DisplayName("Subscriptions différentes → clés différentes")
        void generateKey_differentSubscriptions_shouldProduceDifferentKeys() {
            UUID subId1 = UUID.randomUUID();
            UUID subId2 = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 8, 1);

            String key1 = idempotencyService
                    .generateKey(subId1, date);
            String key2 = idempotencyService
                    .generateKey(subId2, date);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("Clé PRORATION : contient PRORATION + date exacte")
        void generateProrataKey_shouldContainProrationAndDate() {
            LocalDate upgradeDate = LocalDate.of(2026, 8, 15);

            String key = idempotencyService.generateProrataKey(
                    subscriptionId, upgradeDate
            );

            assertThat(key)
                    .contains(subscriptionId.toString())
                    .contains("PRORATION")
                    .contains("2026-08-15");
        }

        @Test
        @DisplayName("Clé PRORATION différente chaque jour")
        void generateProrataKey_differentDays_shouldProduceDifferentKeys() {
            LocalDate day1 = LocalDate.of(2026, 8, 10);
            LocalDate day2 = LocalDate.of(2026, 8, 11);

            String key1 = idempotencyService
                    .generateProrataKey(subscriptionId, day1);
            String key2 = idempotencyService
                    .generateProrataKey(subscriptionId, day2);

            assertThat(key1).isNotEqualTo(key2);
        }
    }

    // ════════════════════════════════════
    // VÉRIFICATION D'EXISTENCE
    // ════════════════════════════════════

    @Nested
    @DisplayName("Vérification d'existence")
    class ExistenceTests {

        @Test
        @DisplayName("Facture existante → invoiceAlreadyExists retourne true")
        void invoiceAlreadyExists_whenExists_shouldReturnTrue() {
            // GIVEN
            String key = "some-key-2026-08";
            when(invoiceRepository.existsByIdempotencyKey(key))
                    .thenReturn(true);

            // WHEN
            boolean result = idempotencyService
                    .invoiceAlreadyExists(key);

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Facture inexistante → invoiceAlreadyExists retourne false")
        void invoiceAlreadyExists_whenNotExists_shouldReturnFalse() {
            // GIVEN
            String key = "some-key-2026-09";
            when(invoiceRepository.existsByIdempotencyKey(key))
                    .thenReturn(false);

            // WHEN
            boolean result = idempotencyService
                    .invoiceAlreadyExists(key);

            // THEN
            assertThat(result).isFalse();
        }
    }
}