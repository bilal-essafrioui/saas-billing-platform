package com.saas.billing.dunning_service.repository;

import com.saas.billing.dunning_service.domain.entity.DunningAttempt;
import com.saas.billing.dunning_service.domain.enums.DunningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DunningAttemptRepository
        extends JpaRepository<DunningAttempt, UUID> {

    // ════════════════════════════════════
    // SCHEDULER
    // trouver les relances à exécuter aujourd'hui
    // ════════════════════════════════════

    List<DunningAttempt> findByStatusAndScheduledDate(
            DunningStatus status,
            LocalDate date
    );

    // ════════════════════════════════════
    // ANNULER TOUTES LES RELANCES
    // quand paiement réussi entre-temps
    // ════════════════════════════════════

    @Modifying
    @Query("UPDATE DunningAttempt d " +
            "SET d.status = 'CANCELLED' " +
            "WHERE d.subscriptionId = :subscriptionId " +
            "AND d.status = 'SCHEDULED'")
    void cancelAllScheduledBySubscriptionId(
            @Param("subscriptionId") UUID subscriptionId
    );

    // ════════════════════════════════════
    // VÉRIFIER SI DÉJÀ UNE RELANCE ACTIVE
    // idempotence
    // ════════════════════════════════════

    boolean existsByInvoiceIdAndAttemptNumberAndStatusNot(
            UUID invoiceId,
            int attemptNumber,
            DunningStatus status
    );

    // ════════════════════════════════════
    // COMPTER LES TENTATIVES ÉCHOUÉES
    // pour savoir si on a atteint J+7
    // ════════════════════════════════════

    long countByInvoiceIdAndStatus(
            UUID invoiceId,
            DunningStatus status
    );

    // ════════════════════════════════════
    // HISTORIQUE PAR INVOICE
    // ════════════════════════════════════

    List<DunningAttempt> findByInvoiceIdOrderByAttemptNumberAsc(
            UUID invoiceId
    );

    // ════════════════════════════════════
    // HISTORIQUE PAR SUBSCRIPTION
    // ════════════════════════════════════

    List<DunningAttempt> findBySubscriptionIdOrderByCreatedAtDesc(
            UUID subscriptionId
    );
}