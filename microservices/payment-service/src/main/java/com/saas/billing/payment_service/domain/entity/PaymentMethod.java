package com.saas.billing.payment_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Stripe Customer ID
    // créé par Stripe lors du premier paiement
    // ex : cus_xxx
    @Column(name = "stripe_customer_id", nullable = false)
    private String stripeCustomerId;

    // Stripe PaymentMethod ID
    // ex : pm_xxx
    @Column(name = "stripe_payment_method_id", nullable = false)
    private String stripePaymentMethodId;

    // infos de la carte pour affichage
    // jamais le vrai numéro
    @Column(name = "brand")
    private String brand;       // visa, mastercard

    @Column(name = "last4")
    private String last4;       // 4242

    @Column(name = "exp_month")
    private Integer expMonth;   // 12

    @Column(name = "exp_year")
    private Integer expYear;    // 2028

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
