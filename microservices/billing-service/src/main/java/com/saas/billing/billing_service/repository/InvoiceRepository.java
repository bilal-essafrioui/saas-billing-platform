package com.saas.billing.billing_service.repository;

import com.saas.billing.billing_service.domain.entity.Invoice;
import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository
        extends JpaRepository<Invoice, UUID> {

    // ════════════════════════════════════
    // IDEMPOTENCE
    // vérifier si facture déjà créée
    // ════════════════════════════════════

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Invoice> findByIdempotencyKey(String idempotencyKey);

    // ════════════════════════════════════
    // CLIENT
    // ses propres factures
    // ════════════════════════════════════

    List<Invoice> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Invoice> findBySubscriptionIdOrderByCreatedAtDesc(
            UUID subscriptionId
    );

    // ════════════════════════════════════
    // ADMIN
    // toutes les factures
    // ════════════════════════════════════

    List<Invoice> findAllByOrderByCreatedAtDesc();

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByType(InvoiceType type);

    // ════════════════════════════════════
    // STATS
    // ════════════════════════════════════

    long countByStatus(InvoiceStatus status);
}