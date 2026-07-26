package com.saas.billing.dunning_service.domain.entity;

import com.saas.billing.dunning_service.domain.enums.DunningStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dunning_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DunningAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "amount",
            nullable = false,
            precision = 10,
            scale = 2)
    private BigDecimal amount;

    @Column(name = "currency",
            nullable = false,
            length = 3)
    private String currency;

    // numéro de tentative
    // 1 = J+1, 2 = J+3, 3 = J+7
    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DunningStatus status;

    // date à laquelle la relance doit être exécutée
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    // rempli quand la relance est exécutée
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    // raison de l'échec si FAILED
    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}