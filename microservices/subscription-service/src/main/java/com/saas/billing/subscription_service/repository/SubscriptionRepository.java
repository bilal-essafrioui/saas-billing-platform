package com.saas.billing.subscription_service.repository;

import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.domain.entity.UserCache;
import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, UUID> {

    // ════════════════════════════════════
    // Find Subscription of a user
    // One line per user
    // ════════════════════════════════════

    Optional<Subscription> findByUser(UserCache user);

    // ════════════════════════════════════
    // VERIFY IF A USER HAS A SUBSCRIPTION
    // used before create/resubscribe
    // ════════════════════════════════════

    boolean existsByUser(UserCache user);

    // ════════════════════════════════════
    // Verify if subscription is ACTIVE
    // Status = ACTIVE ou PAST_DUE
    // ════════════════════════════════════

    @Query("SELECT COUNT(s) > 0 FROM Subscription s " +
            "WHERE s.user = :user " +
            "AND s.status IN ('ACTIVE', 'PAST_DUE')")
    boolean isSubscriptionActive(@Param("user") UserCache user);

    // ════════════════════════════════════
    // FIND SUBSCRIPTIONS BY STATUS
    // dashboard admin
    // ════════════════════════════════════

    List<Subscription> findByStatus(SubscriptionStatus status);

    // ════════════════════════════════════
    // SCHEDULER subscription
    // ACTIVE SUBSCRIPTIONS TO Schedule today
    // ════════════════════════════════════

    List<Subscription> findByStatusAndNextRenewalDate(
            SubscriptionStatus status,
            LocalDate date
    );

    // ════════════════════════════════════
    // SCHEDULER DOWNGRADE
    // ════════════════════════════════════

    @Query("SELECT s FROM Subscription s " +
            "WHERE s.pendingPlanId IS NOT NULL " +
            "AND s.pendingPlanEffectiveDate <= :date")
    List<Subscription> findPendingDowngrades(
            @Param("date") LocalDate date
    );

    // ════════════════════════════════════
    // STATS DASHBOARD ADMIN
    // ════════════════════════════════════

    long countByStatus(SubscriptionStatus status);

    // ════════════════════════════════════
    // SUBSCRIPTIONS PAST_DUE + SUSPENDED
    // alertes dashboard admin
    // ════════════════════════════════════

    @Query("SELECT s FROM Subscription s " +
            "WHERE s.status IN ('PAST_DUE', 'SUSPENDED')")
    List<Subscription> findAtRiskSubscriptions();
}