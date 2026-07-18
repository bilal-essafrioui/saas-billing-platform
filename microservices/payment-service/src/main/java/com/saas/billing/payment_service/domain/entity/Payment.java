package com.saas.billing.payment_service.domain.entity;

import com.saas.billing.payment_service.domain.enums.PaymentStatus;
import com.saas.billing.payment_service.domain.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    // null pour INITIAL_SUBSCRIPTION
    // rempli pour RENEWAL et RETRY
    @Column(name = "invoice_id")
    private UUID invoiceId;

    // référence vers payment_methods
    @Column(name = "payment_method_id")
    private UUID paymentMethodId;

    @Column(name = "amount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal amount;

    @Column(name = "currency",
            nullable = false,
            length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // Stripe PaymentIntent ID
    // ex : pi_xxx
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    // clé unique par tentative
    // évite les doubles débits
    @Column(name = "idempotency_key",
            unique = true,
            nullable = false)
    private String idempotencyKey;

    // raison de l'échec si FAILED
    // ex : insufficient_funds, card_declined
    @Column(name = "failure_reason")
    private String failureReason;

    // numéro de tentative
    // 1 pour premier essai
    // 2, 3, 4 pour les relances dunning
    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;

    // null si pas encore payé
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}