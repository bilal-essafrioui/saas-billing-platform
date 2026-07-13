package com.saas.billing.subscription_service.domain.entity;

import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.domain.enums.SubscriptionEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // le type de changement
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private SubscriptionEventType eventType;

    // plan before change
    @Column(name = "previous_plan_id")
    private UUID previousPlanId;

    @Column(name = "previous_plan_name")
    private String previousPlanName;

    @Column(name = "previous_plan_price", precision = 10, scale = 2)
    private BigDecimal previousPlanPrice;

    // plan after change
    @Column(name = "new_plan_id")
    private UUID newPlanId;

    @Column(name = "new_plan_name")
    private String newPlanName;

    @Column(name = "new_plan_price", precision = 10, scale = 2)
    private BigDecimal newPlanPrice;

    // previous status
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private SubscriptionStatus previousStatus;

    // new status
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private SubscriptionStatus newStatus;

    // Proration amount if upgrade
    @Column(name = "proration_amount", precision = 10, scale = 2)
    private BigDecimal prorataAmount;

    // optional amount
    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}