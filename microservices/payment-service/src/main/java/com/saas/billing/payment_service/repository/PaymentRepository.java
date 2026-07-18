package com.saas.billing.payment_service.repository;

import com.saas.billing.payment_service.domain.entity.Payment;
import com.saas.billing.payment_service.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    // trouver par stripePaymentIntentId
    // utilisé par le webhook handler
    Optional<Payment> findByStripePaymentIntentId(
            String stripePaymentIntentId
    );

    // trouver par idempotencyKey
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    // historique des paiements d'un user
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // paiements par invoice
    List<Payment> findByInvoiceIdOrderByCreatedAtDesc(UUID invoiceId);

    // tous les paiements (admin)
    List<Payment> findAllByOrderByCreatedAtDesc();

    // paiements échoués (admin)
    List<Payment> findByStatusOrderByCreatedAtDesc(
            PaymentStatus status
    );

    // vérifier idempotence
    boolean existsByIdempotencyKey(String idempotencyKey);
}