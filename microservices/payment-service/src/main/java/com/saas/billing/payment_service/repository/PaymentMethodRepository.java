package com.saas.billing.payment_service.repository;

import com.saas.billing.payment_service.domain.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository
        extends JpaRepository<PaymentMethod, UUID> {

    // trouver le moyen de paiement actif d'un user
    Optional<PaymentMethod> findByUserIdAndActiveTrue(UUID userId);

    // vérifier si un user a déjà un moyen de paiement
    boolean existsByUserIdAndActiveTrue(UUID userId);

    // trouver par stripeCustomerId
    Optional<PaymentMethod> findByStripeCustomerId(
            String stripeCustomerId
    );

    Optional<PaymentMethod> findByStripeCustomerIdAndActiveTrue(
            String stripeCustomerId
    );
}
