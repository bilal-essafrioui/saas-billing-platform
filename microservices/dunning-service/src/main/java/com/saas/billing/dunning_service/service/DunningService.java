package com.saas.billing.dunning_service.service;

import com.saas.billing.dunning_service.domain.entity.DunningAttempt;
import com.saas.billing.dunning_service.domain.enums.DunningStatus;
import com.saas.billing.dunning_service.dto.request.RetryPaymentRequest;
import com.saas.billing.dunning_service.messaging.producer.DunningEventPublisher;
import com.saas.billing.dunning_service.repository.DunningAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DunningService {

    private final DunningAttemptRepository dunningRepository;
    private final DunningEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    @Value("${payment-service.url}")
    private String paymentServiceUrl;

    // nombre maximum de relances
    // J+1, J+3, J+7 = 3 relances
    private static final int MAX_ATTEMPTS = 3;

    // jours de relance
    private static final int[] RETRY_DAYS = {1, 3, 7};

    public DunningService(
            DunningAttemptRepository dunningRepository,
            DunningEventPublisher eventPublisher,
            RestTemplate restTemplate) {
        this.dunningRepository = dunningRepository;
        this.eventPublisher = eventPublisher;
        this.restTemplate = restTemplate;
    }

    // ════════════════════════════════════
    // PLANIFIER UNE RELANCE
    // appelé quand payment-failed est reçu
    // ════════════════════════════════════

    @Transactional
    public void scheduleRetry(
            UUID subscriptionId,
            UUID invoiceId,
            UUID userId,
            String userEmail,
            UUID planId,
            BigDecimal amount,
            String currency,
            int failedAttemptNumber) {

        // vérifier si on a encore des relances disponibles
        // failedAttemptNumber = numéro de la tentative qui vient d'échouer
        // on planifie la SUIVANTE
        int nextAttemptNumber = failedAttemptNumber + 1;

        if (nextAttemptNumber > MAX_ATTEMPTS) {
            // toutes les relances épuisées
            // suspendre le compte
            suspendSubscription(
                    subscriptionId, userId, userEmail
            );
            return;
        }

        // idempotence → éviter de planifier deux fois
        if (dunningRepository
                .existsByInvoiceIdAndAttemptNumberAndStatusNot(
                        invoiceId,
                        nextAttemptNumber,
                        DunningStatus.CANCELLED
                )) {
            return;
        }

        // calculer la date de la prochaine relance
        // RETRY_DAYS[0] = 1 jour → J+1
        // RETRY_DAYS[1] = 3 jours → J+3
        // RETRY_DAYS[2] = 7 jours → J+7
        int daysToAdd = RETRY_DAYS[nextAttemptNumber - 1];
        LocalDate scheduledDate = LocalDate.now()
                .plusDays(daysToAdd);

        DunningAttempt attempt = DunningAttempt.builder()
                .subscriptionId(subscriptionId)
                .invoiceId(invoiceId)
                .userId(userId)
                .userEmail(userEmail)
                .planId(planId)
                .amount(amount)
                .currency(currency)
                .attemptNumber(nextAttemptNumber)
                .status(DunningStatus.SCHEDULED)
                .scheduledDate(scheduledDate)
                .build();

        dunningRepository.save(attempt);

        System.out.println(
                "Retry scheduled for invoice "
                        + invoiceId
                        + " → attempt " + nextAttemptNumber
                        + " on " + scheduledDate
        );
    }

    // ════════════════════════════════════
    // EXÉCUTER LES RELANCES DU JOUR
    // appelé par le scheduler chaque nuit
    // ════════════════════════════════════

    @Transactional
    public void executeScheduledRetries() {
        LocalDate today = LocalDate.now();

        List<DunningAttempt> todayRetries =
                dunningRepository.findByStatusAndScheduledDate(
                        DunningStatus.SCHEDULED,
                        today
                );

        System.out.println(
                "Found " + todayRetries.size()
                        + " retries to execute today"
        );

        for (DunningAttempt attempt : todayRetries) {
            executeRetry(attempt);
        }
    }

    // ════════════════════════════════════
    // ANNULER LES RELANCES
    // appelé quand payment-succeeded reçu
    // ════════════════════════════════════

    @Transactional
    public void cancelRetries(UUID subscriptionId) {
        dunningRepository
                .cancelAllScheduledBySubscriptionId(subscriptionId);

        System.out.println(
                "Cancelled all retries for subscription : "
                        + subscriptionId
        );
    }

    // ════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ════════════════════════════════════

    private void executeRetry(DunningAttempt attempt) {
        // marquer comme en cours
        attempt.setStatus(DunningStatus.PROCESSING);
        attempt.setExecutedAt(LocalDateTime.now());
        dunningRepository.save(attempt);

        try {
            // appeler payment-service via HTTP
            // payment-service va débiter la carte
            // avec off-session PaymentIntent
            RetryPaymentRequest request =
                    RetryPaymentRequest.builder()
                            .userId(attempt.getUserId())
                            .planId(attempt.getPlanId())
                            .invoiceId(attempt.getInvoiceId())
                            .amount(attempt.getAmount())
                            .currency(attempt.getCurrency())
                            .paymentType("RETRY")
                            .attemptNumber(attempt.getAttemptNumber())
                            .build();

            String url = paymentServiceUrl
                    + "/internal/payments/create-intent";

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url, request, String.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {
                // paiement déclenché avec succès
                // le résultat arrivera via webhook Stripe
                // → payment-service publiera PaymentSucceeded
                //   ou PaymentFailed sur Kafka
                System.out.println(
                        "Retry triggered for invoice : "
                                + attempt.getInvoiceId()
                                + " attempt : "
                                + attempt.getAttemptNumber()
                );
            } else {
                handleRetryFailure(
                        attempt,
                        "Payment service returned error"
                );
            }

        } catch (Exception e) {
            handleRetryFailure(attempt, e.getMessage());
        }
    }

    private void handleRetryFailure(
            DunningAttempt attempt,
            String reason) {

        attempt.setStatus(DunningStatus.FAILED);
        attempt.setFailureReason(reason);
        dunningRepository.save(attempt);

        System.err.println(
                "Retry failed for invoice "
                        + attempt.getInvoiceId()
                        + " : " + reason
        );
    }

    private void suspendSubscription(
            UUID subscriptionId,
            UUID userId,
            String userEmail) {

        System.out.println(
                "Suspending subscription : " + subscriptionId
                        + " after all retries exhausted"
        );

        // publier sur Kafka
        // subscription-service écoute → met SUSPENDED
        // notification-service écoute → envoie email
        eventPublisher.publishSubscriptionSuspended(
                subscriptionId,
                userId,
                userEmail
        );
    }
}