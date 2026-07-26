package com.saas.billing.billing_service.repository;

import com.saas.billing.billing_service.domain.entity.Invoice;
import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    List<Invoice> findByUserIdAndStatus(UUID userId, InvoiceStatus status);

    List<Invoice> findByType(InvoiceType type);

    @Query("""
        SELECT i
        FROM Invoice i
        WHERE i.userId = :userId
          AND (:status IS NULL OR i.status = :status)
          AND (CAST(:startDate AS date) IS NULL 
               OR i.billingPeriodStart >= :startDate)
        ORDER BY i.createdAt DESC
    """)
    List<Invoice> filterInvoices(
            @Param("userId") UUID userId,
            @Param("status") InvoiceStatus status,
            @Param("startDate") LocalDate startDate
    );

    // ════════════════════════════════════
    // STATS
    // ════════════════════════════════════

    long countByStatus(InvoiceStatus status);
}