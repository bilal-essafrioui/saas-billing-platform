package com.saas.billing.dunning_service.service;

import com.saas.billing.dunning_service.domain.entity.DunningAttempt;
import com.saas.billing.dunning_service.domain.enums.DunningStatus;
import com.saas.billing.dunning_service.messaging.producer.DunningEventPublisher;
import com.saas.billing.dunning_service.repository.DunningAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DunningService Tests")
class DunningServiceTest {

    @Mock
    private DunningAttemptRepository dunningRepository;

    @Mock
    private DunningEventPublisher eventPublisher;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DunningService dunningService;

    private UUID subscriptionId;
    private UUID invoiceId;
    private UUID userId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();

        ReflectionTestUtils.setField(
                dunningService,
                "paymentServiceUrl",
                "http://localhost:8084"
        );
    }

    // ════════════════════════════════════
    // PLANIFICATION DES RELANCES
    // ════════════════════════════════════

    @Nested
    @DisplayName("Planification des relances")
    class ScheduleRetryTests {

        @Test
        @DisplayName("Échec J+0 (attempt=1) → planifie relance à J+1")
        void failure_attempt1_shouldScheduleRetryAtDayPlus1() {
            // GIVEN
            when(dunningRepository
                    .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                            any(), anyInt(), any()
                    ))
                    .thenReturn(false);

            when(dunningRepository.save(any()))
                    .thenAnswer(i -> i.getArgument(0));

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    new BigDecimal("30.00"), "USD",
                    0 // tentative 1 vient d'échouer
            );

            // THEN
            ArgumentCaptor<DunningAttempt> captor =
                    ArgumentCaptor.forClass(DunningAttempt.class);
            verify(dunningRepository).save(captor.capture());

            DunningAttempt saved = captor.getValue();
            assertThat(saved.getAttemptNumber()).isEqualTo(1);
            assertThat(saved.getScheduledDate())
                    .isEqualTo(LocalDate.now().plusDays(1));
            assertThat(saved.getStatus())
                    .isEqualTo(DunningStatus.SCHEDULED);
        }

        @Test
        @DisplayName("Échec J+1 (attempt=2) → planifie relance à J+3")
        void failure_attempt2_shouldScheduleRetryAtDayPlus3() {
            // GIVEN
            when(dunningRepository
                    .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                            any(), anyInt(), any()
                    ))
                    .thenReturn(false);
            when(dunningRepository.save(any()))
                    .thenAnswer(i -> i.getArgument(0));

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    new BigDecimal("30.00"), "USD",
                    1 // tentative 2 vient d'échouer
            );

            // THEN
            ArgumentCaptor<DunningAttempt> captor =
                    ArgumentCaptor.forClass(DunningAttempt.class);
            verify(dunningRepository).save(captor.capture());

            DunningAttempt saved = captor.getValue();
            assertThat(saved.getAttemptNumber()).isEqualTo(2);
            assertThat(saved.getScheduledDate())
                    .isEqualTo(LocalDate.now().plusDays(3));
        }

        @Test
        @DisplayName("Échec J+3 (attempt=3) → planifie relance à J+7")
        void failure_attempt3_shouldScheduleRetryAtDayPlus7() {
            // GIVEN
            when(dunningRepository
                    .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                            any(), anyInt(), any()
                    ))
                    .thenReturn(false);
            when(dunningRepository.save(any()))
                    .thenAnswer(i -> i.getArgument(0));

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    new BigDecimal("30.00"), "USD",
                    2 // tentative 3 vient d'échouer
            );

            // THEN
            ArgumentCaptor<DunningAttempt> captor =
                    ArgumentCaptor.forClass(DunningAttempt.class);
            verify(dunningRepository).save(captor.capture());

            DunningAttempt saved = captor.getValue();
            assertThat(saved.getAttemptNumber()).isEqualTo(3);
            assertThat(saved.getScheduledDate())
                    .isEqualTo(LocalDate.now().plusDays(7));
        }

        @Test
        @DisplayName("Échec J+7 (attempt=4) → suspension publiée")
        void failure_attempt4_shouldPublishSuspension() {
            // GIVEN
            // MAX_ATTEMPTS = 3
            // attemptNumber 4 > MAX_ATTEMPTS
            // → suspendre

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    new BigDecimal("30.00"), "eur",
                    4 // J+7 vient d'échouer → MAX atteint
            );

            // THEN
            // suspension publiée sur Kafka
            verify(eventPublisher).publishSubscriptionSuspended(
                    subscriptionId,
                    userId,
                    "ahmed@email.com"
            );

            // PAS de nouvelle relance planifiée
            verify(dunningRepository, never()).save(any());
        }

        @Test
        @DisplayName("IDEMPOTENCE : relance déjà planifiée → pas de doublon")
        void scheduleRetry_alreadyExists_shouldNotCreateDuplicate() {
            // GIVEN
            when(dunningRepository
                    .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                            invoiceId, 2, DunningStatus.CANCELLED
                    ))
                    .thenReturn(true); // déjà planifiée

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    new BigDecimal("30.00"), "eur",
                    1
            );

            // THEN — PAS de save
            verify(dunningRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════
    // ANNULATION DES RELANCES
    // ════════════════════════════════════

    @Nested
    @DisplayName("Annulation des relances")
    class CancelRetryTests {

        @Test
        @DisplayName("Paiement réussi → toutes les relances annulées")
        void paymentSucceeded_shouldCancelAllRetries() {
            // WHEN
            dunningService.cancelRetries(subscriptionId);

            // THEN
            verify(dunningRepository)
                    .cancelAllScheduledBySubscriptionId(
                            subscriptionId
                    );
        }

        @Test
        @DisplayName("Annulation → pas de suspension publiée")
        void cancelRetries_shouldNotPublishSuspension() {
            // WHEN
            dunningService.cancelRetries(subscriptionId);

            // THEN
            verify(eventPublisher, never())
                    .publishSubscriptionSuspended(
                            any(), any(), any()
                    );
        }
    }

    // ════════════════════════════════════
    // DONNÉES DE LA RELANCE
    // ════════════════════════════════════

    @Nested
    @DisplayName("Données de la relance")
    class RetryDataTests {

        @Test
        @DisplayName("Relance stocke le bon montant")
        void retry_shouldStoreCorrectAmount() {
            // GIVEN
            when(dunningRepository
                    .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                            any(), anyInt(), any()
                    ))
                    .thenReturn(false);
            when(dunningRepository.save(any()))
                    .thenAnswer(i -> i.getArgument(0));

            BigDecimal amount = new BigDecimal("99.00");

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    amount, "eur", 1
            );

            // THEN
            verify(dunningRepository).save(
                    argThat(attempt ->
                            attempt.getAmount()
                                    .compareTo(amount) == 0
                    )
            );
        }

        @Test
        @DisplayName("Relance stocke le bon subscriptionId")
        void retry_shouldStoreCorrectSubscriptionId() {
            // GIVEN
            when(dunningRepository
                    .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                            any(), anyInt(), any()
                    ))
                    .thenReturn(false);
            when(dunningRepository.save(any()))
                    .thenAnswer(i -> i.getArgument(0));

            // WHEN
            dunningService.scheduleRetry(
                    subscriptionId, invoiceId, userId,
                    "ahmed@email.com", planId,
                    new BigDecimal("30.00"), "eur", 1
            );

            // THEN
            verify(dunningRepository).save(
                    argThat(attempt ->
                            attempt.getSubscriptionId()
                                    .equals(subscriptionId)
                                    && attempt.getInvoiceId()
                                    .equals(invoiceId)
                                    && attempt.getUserId()
                                    .equals(userId)
                    )
            );
        }
    }
}