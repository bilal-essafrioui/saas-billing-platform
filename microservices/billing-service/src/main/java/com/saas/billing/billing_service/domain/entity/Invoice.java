package com.saas.billing.billing_service.domain.entity;

import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // référence vers subscription-service
    // pas de FK inter-service
    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    // référence vers auth-service
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // email pour les notifications
    // stocké localement pour éviter d'appeler auth-service
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "amount", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InvoiceType type;

    // clé unique : subscriptionId + mois + année
    // garantit l'idempotence
    @Column(name = "idempotency_key",
            nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    // null tant que pas payée
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}