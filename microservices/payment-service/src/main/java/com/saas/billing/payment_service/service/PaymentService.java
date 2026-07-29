package com.saas.billing.payment_service.service;

import com.saas.billing.payment_service.domain.entity.Payment;
import com.saas.billing.payment_service.domain.entity.PaymentMethod;
import com.saas.billing.payment_service.domain.enums.PaymentStatus;
import com.saas.billing.payment_service.domain.enums.PaymentType;
import com.saas.billing.payment_service.dto.request.ChargePaymentRequest;
import com.saas.billing.payment_service.dto.request.CreatePaymentIntentRequest;
import com.saas.billing.payment_service.dto.response.ChargePaymentResponse;
import com.saas.billing.payment_service.dto.response.CreatePaymentIntentResponse;
import com.saas.billing.payment_service.dto.response.CreateSetupIntentResponse;
import com.saas.billing.payment_service.dto.response.PaymentResponse;
import com.saas.billing.payment_service.exception.PaymentMethodNotFoundException;
import com.saas.billing.payment_service.exception.PaymentNotFoundException;
import com.saas.billing.payment_service.messaging.event.FirstInvoiceCreatedEvent;
import com.saas.billing.payment_service.messaging.producer.PaymentEventPublisher;
import com.saas.billing.payment_service.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodService paymentMethodService;
    private final StripeService stripeService;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentMethodService paymentMethodService,
            StripeService stripeService,
            PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodService = paymentMethodService;
        this.stripeService = stripeService;
        this.eventPublisher = eventPublisher;
    }

    // ════════════════════════════════════
    // FLUX 1 — INITIAL SUBSCRIPTION
    // appelé par subscription-service via HTTP
    // retourne clientSecret au frontend
    // ════════════════════════════════════

    @Transactional
    public CreatePaymentIntentResponse createPaymentIntent(
            CreatePaymentIntentRequest request,
            String userEmail) throws StripeException {

        String idempotencyKey = buildIdempotencyKey(
                request.getUserId(),
                request.getPlanId(),
                request.getInvoiceId(),
                request.getAttemptNumber()
        );

        // idempotence → si déjà créé retourner l'existant
        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            Payment existing = paymentRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow();
            return CreatePaymentIntentResponse.builder()
                    .clientSecret(null)
                    .paymentId(existing.getId())
                    .paymentIntentId(
                            existing.getStripePaymentIntentId()
                    )
                    .build();
        }

        // créer ou récupérer le Stripe Customer
        String stripeCustomerId;
        if (paymentMethodService.hasPaymentMethod(
                request.getUserId())) {
            PaymentMethod pm = paymentMethodService
                    .getPaymentMethodEntity(request.getUserId());
            stripeCustomerId = pm.getStripeCustomerId();
        } else {
            stripeCustomerId = stripeService.createCustomer(
                    userEmail,
                    request.getUserId().toString()
            );
        }

        // créer le PaymentIntent Stripe
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                request.getAmount(),
                request.getCurrency(),
                stripeCustomerId,
                idempotencyKey
        );

        // stocker le paiement en base
        Payment payment = Payment.builder()
                .userId(request.getUserId())
                .userEmail(userEmail)
                .planId(request.getPlanId())
                .invoiceId(request.getInvoiceId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentType(request.getPaymentType())
                .status(PaymentStatus.PENDING)
                .stripePaymentIntentId(paymentIntent.getId())
                .idempotencyKey(idempotencyKey)
                .attemptNumber(request.getAttemptNumber())
                .build();

        payment = paymentRepository.save(payment);

        return CreatePaymentIntentResponse.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .paymentId(payment.getId())
                .paymentIntentId(paymentIntent.getId())
                .build();
    }

    //
    @Transactional
    public void linkInvoiceAndSubscription(
            FirstInvoiceCreatedEvent event
    ) {

        Payment payment = paymentRepository
                .findById(event.paymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found : "
                                        + event.paymentId()
                        )
                );

        payment.setInvoiceId(event.invoiceId());
        payment.setSubscriptionId(event.subscriptionId());

        paymentRepository.save(payment);
    }

    // ════════════════════════════════════
    // FLUX 2 — RENEWAL / RETRY
    // appelé par InvoiceEventConsumer (Kafka)
    // débite automatiquement la carte sauvegardée
    // pas besoin du frontend
    // ════════════════════════════════════

    @Transactional
    public void processAutoPayment(
            UUID invoiceId,
            UUID subscriptionId,
            UUID userId,
            UUID planId,
            String userEmail,
            java.math.BigDecimal amount,
            String currency,
            int attemptNumber) {

        // récupérer le moyen de paiement
        PaymentMethod pm;
        try {
            pm = paymentMethodService
                    .getPaymentMethodEntity(userId);
        } catch (PaymentMethodNotFoundException e) {
            // pas de carte → publier PaymentFailed
            eventPublisher.publishPaymentFailed(
                    null,
                    invoiceId,
                    subscriptionId,
                    userId,
                    userEmail,
                    amount,
                    "No payment method found",
                    attemptNumber
            );
            return;
        }

        String idempotencyKey = invoiceId
                + "_attempt_"
                + attemptNumber;

        // idempotence
        if (paymentRepository.existsByIdempotencyKey(
                idempotencyKey)) {
            return;
        }

        PaymentType paymentType = attemptNumber == 1
                ? PaymentType.RENEWAL
                : PaymentType.RETRY;

        try {
            // débiter automatiquement
            PaymentIntent paymentIntent =
                    stripeService.createOffSessionPaymentIntent(
                            amount,
                            currency,
                            pm.getStripeCustomerId(),
                            pm.getStripePaymentMethodId(),
                            idempotencyKey
                    );

            // stocker le paiement
            Payment payment = Payment.builder()
                    .userId(userId)
                    .planId(planId)
                    .invoiceId(invoiceId)
                    .paymentMethodId(pm.getId())
                    .amount(amount)
                    .currency(currency)
                    .paymentType(paymentType)
                    .status(PaymentStatus.PENDING)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .idempotencyKey(idempotencyKey)
                    .attemptNumber(attemptNumber)
                    .build();

            paymentRepository.save(payment);

            // le résultat arrive via webhook Stripe
            // pas ici

        } catch (StripeException e) {
            // erreur Stripe → enregistrer et publier FAILED
            Payment payment = Payment.builder()
                    .userId(userId)
                    .planId(planId)
                    .invoiceId(invoiceId)
                    .paymentMethodId(pm.getId())
                    .amount(amount)
                    .currency(currency)
                    .paymentType(paymentType)
                    .status(PaymentStatus.FAILED)
                    .idempotencyKey(idempotencyKey)
                    .failureReason(e.getMessage())
                    .attemptNumber(attemptNumber)
                    .build();

            paymentRepository.save(payment);

            eventPublisher.publishPaymentFailed(
                    payment.getId(),
                    invoiceId,
                    subscriptionId,
                    userId,
                    userEmail,
                    amount,
                    e.getMessage(),
                    attemptNumber
            );
        }
    }

    //
    @Transactional
    public ChargePaymentResponse chargePayment(
            ChargePaymentRequest request
    ) {
        System.out.println("7. chargePayment");
        PaymentMethod pm = paymentMethodService
                .getPaymentMethodEntity(request.userId());

        String idempotencyKey =
                request.subscriptionId()
                        + "_upgrade_"
                        + request.planId();

        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {

            Payment existing = paymentRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow();

            return new ChargePaymentResponse(
                    existing.getId(),
                    existing.getStatus(),
                    "Payment already processed."
            );
        }

        PaymentIntent paymentIntent;

        try {
            paymentIntent =
                    stripeService.createOffSessionPaymentIntent(
                            request.amount(),
                            request.currency(),
                            pm.getStripeCustomerId(),
                            pm.getStripePaymentMethodId(),
                            idempotencyKey
                    );
        } catch (StripeException e) {

            Payment failedPayment = Payment.builder()
                    .userId(request.userId())
                    .userEmail(request.userEmail())
                    .subscriptionId(request.subscriptionId())
                    .planId(request.planId())
                    .paymentMethodId(pm.getId())
                    .amount(request.amount())
                    .currency(request.currency())
                    .paymentType(request.paymentType())
                    .status(PaymentStatus.FAILED)
                    .failureReason(e.getMessage())
                    .idempotencyKey(idempotencyKey)
                    .attemptNumber(1)
                    .build();

            failedPayment = paymentRepository.save(failedPayment);

            return new ChargePaymentResponse(
                    failedPayment.getId(),
                    PaymentStatus.FAILED,
                    e.getMessage()
            );
        }

        System.out.println("PaymentIntent status = " + paymentIntent.getStatus());

        boolean success =
                "succeeded".equals(paymentIntent.getStatus());

        Payment payment = Payment.builder()
                .userId(request.userId())
                .userEmail(request.userEmail())
                .subscriptionId(request.subscriptionId())
                .planId(request.planId())
                .paymentMethodId(pm.getId())
                .amount(request.amount())
                .currency(request.currency())
                .paymentType(request.paymentType())
                .status(success
                        ? PaymentStatus.SUCCEEDED
                        : PaymentStatus.FAILED)
                .paidAt(success ? LocalDateTime.now() : null)
                .failureReason(success ? null : paymentIntent.getStatus())
                .stripePaymentIntentId(paymentIntent.getId())
                .idempotencyKey(idempotencyKey)
                .attemptNumber(1)
                .build();

        payment = paymentRepository.save(payment);

        if (success) {
            return new ChargePaymentResponse(
                    payment.getId(),
                    PaymentStatus.SUCCEEDED,
                    "Payment completed successfully"
            );
        }

        return new ChargePaymentResponse(
                payment.getId(),
                PaymentStatus.FAILED,
                "Payment failed: " + paymentIntent.getStatus()
        );
    }

    // ════════════════════════════════════
    // WEBHOOK HANDLER
    // appelé par StripeWebhookController
    // quand Stripe confirme le résultat
    // ════════════════════════════════════

    @Transactional
    public void handlePaymentSucceeded(
            String paymentIntentId,
            String stripeCustomerId,
            String stripePaymentMethodId,
            String brand,
            String last4,
            Integer expMonth,
            Integer expYear) {

        System.out.println("Searching payment...");

        Payment payment = paymentRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for intent : "
                                + paymentIntentId
                ));

        System.out.println("Payment found : " + payment.getId());

        System.out.println("Updating payment...");

        // mettre à jour le statut
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        System.out.println("Payment updated.");

        System.out.println("Saving payment method...");
        // sauvegarder le moyen de paiement
        // pour les prochains renouvellements
        paymentMethodService.savePaymentMethod(
                payment.getUserId(),
                stripeCustomerId,
                stripePaymentMethodId,
                brand,
                last4,
                expMonth,
                expYear
        );
        System.out.println("Payment method saved.");

        System.out.println("Publishing Kafka event...");
        // publier sur Kafka
        eventPublisher.publishPaymentSucceeded(
                payment.getId(),
                payment.getInvoiceId(),
                null, // subscriptionId récupéré par subscription-service
                payment.getUserId(),
                payment.getUserEmail(), // userEmail récupéré par notification-service
                payment.getPlanId(),
                payment.getAmount(),
                payment.getCurrency()
        );
        System.out.println("Webhook finished.");
    }

    @Transactional
    public void handlePaymentFailed(
            String paymentIntentId,
            String failureReason) {

        Payment payment = paymentRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for intent : "
                                + paymentIntentId
                ));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        paymentRepository.save(payment);

        eventPublisher.publishPaymentFailed(
                payment.getId(),
                payment.getInvoiceId(),
                null,
                payment.getUserId(),
                payment.getUserEmail(),
                payment.getAmount(),
                failureReason,
                payment.getAttemptNumber()
        );
    }
    // ════════════════════════════════════
    // FLUX 4 — UPDATE PAYMENT METHOD
    // crée un SetupIntent Stripe
    // retourne le clientSecret au frontend
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public CreateSetupIntentResponse createSetupIntent(
            UUID userId) throws StripeException {

        // récupérer le Stripe Customer existant
        PaymentMethod paymentMethod =
                paymentMethodService.getPaymentMethodEntity(userId);

        // créer le SetupIntent
        SetupIntent setupIntent =
                stripeService.createSetupIntent(
                        paymentMethod.getStripeCustomerId()
                );

        // retourner le clientSecret
        return CreateSetupIntentResponse.builder()
                .clientSecret(setupIntent.getClientSecret())
                .build();
    }

    // ════════════════════════════════════
    // WEBHOOK HANDLER
    // appelé après un SetupIntent réussi
    // mise à jour du moyen de paiement
    // ════════════════════════════════════

    @Transactional
    public void handleSetupIntentSucceeded(
            String stripeCustomerId,
            String stripePaymentMethodId,
            String brand,
            String last4,
            Integer expMonth,
            Integer expYear) {

        System.out.println("Searching current payment method...");

        PaymentMethod currentPaymentMethod =
                paymentMethodService.getByStripeCustomerId(
                        stripeCustomerId
                );

        System.out.println("Current payment method found.");

        System.out.println("Replacing payment method...");

        paymentMethodService.replacePaymentMethod(
                currentPaymentMethod.getUserId(),
                stripeCustomerId,
                stripePaymentMethodId,
                brand,
                last4,
                expMonth,
                expYear
        );

        System.out.println("Payment method updated successfully.");
    }

    // ════════════════════════════════════
    // LECTURE
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(UUID userId) {
        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getFailedPayments() {
        return paymentRepository
                .findByStatusOrderByCreatedAtDesc(
                        PaymentStatus.FAILED
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ════════════════════════════════════

    private String buildIdempotencyKey(
            UUID userId,
            UUID planId,
            UUID invoiceId,
            int attemptNumber) {

        if (invoiceId != null) {
            return invoiceId + "_attempt_" + attemptNumber;
        }
        return userId + "_" + planId + "_initial";
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .planId(p.getPlanId())
                .invoiceId(p.getInvoiceId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .paymentType(p.getPaymentType())
                .status(p.getStatus())
                .failureReason(p.getFailureReason())
                .attemptNumber(p.getAttemptNumber())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}