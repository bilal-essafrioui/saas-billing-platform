package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.entity.Plan;
import com.saas.billing.subscription_service.domain.entity.Subscription;
import com.saas.billing.subscription_service.domain.entity.UserCache;
import com.saas.billing.subscription_service.domain.enums.SubscriptionEventType;
import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.dto.request.ChangePlanRequest;
import com.saas.billing.subscription_service.dto.request.SubscribeRequest;
import com.saas.billing.subscription_service.dto.response.AdminSubscriptionResponse;
import com.saas.billing.subscription_service.dto.response.DashboardStatsResponse;
import com.saas.billing.subscription_service.dto.response.ProrataResponse;
import com.saas.billing.subscription_service.dto.response.SubscriptionResponse;
import com.saas.billing.subscription_service.exception.InvalidStateTransitionException;
import com.saas.billing.subscription_service.exception.PlanNotFoundException;
import com.saas.billing.subscription_service.exception.SubscriptionAlreadyExistsException;
import com.saas.billing.subscription_service.exception.SubscriptionNotFoundException;
import com.saas.billing.subscription_service.messaging.producer.SubscriptionEventPublisher;
import com.saas.billing.subscription_service.repository.PlanRepository;
import com.saas.billing.subscription_service.repository.SubscriptionRepository;
import com.saas.billing.subscription_service.repository.UserCacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserCacheRepository userCacheRepository;
    private final SubscriptionStateMachineService stateMachine;
    private final ProrataCalculatorService prorataCalculator;
    private final SubscriptionEventService eventService;
    private final SubscriptionEventPublisher eventPublisher;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository,
            UserCacheRepository userCacheRepository,
            SubscriptionStateMachineService stateMachine,
            ProrataCalculatorService prorataCalculator,
            SubscriptionEventService eventService,
            SubscriptionEventPublisher eventPublisher) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userCacheRepository = userCacheRepository;
        this.stateMachine = stateMachine;
        this.prorataCalculator = prorataCalculator;
        this.eventService = eventService;
        this.eventPublisher = eventPublisher;
    }

    // ════════════════════════════════════
    // SUBSCRIBE
    // new subscription or resubscription
    // ════════════════════════════════════
    @Transactional
    public SubscriptionResponse subscribe(
            UUID userId,
            SubscribeRequest request) {

        UserCache user = getUserCache(userId);
        Plan plan = getActivePlan(request.planId());

        // cas 1 : user a déjà une subscription
        if (subscriptionRepository.existsByUser(user)) {
            Subscription existing = subscriptionRepository
                    .findByUser(user)
                    .orElseThrow();

            // si CANCELLED → resubscribe sur la même ligne
            if (existing.getStatus() == SubscriptionStatus.CANCELLED) {
                return resubscribe(existing, plan);
            }

            // if not CANCELLED → already active
            throw new SubscriptionAlreadyExistsException(
                    "You already have an active subscription"
            );
        }

        // cas 2 : first subscription
        return createNewSubscription(user, plan);
    }

    // ════════════════════════════════════
    // GET MY SUBSCRIPTION
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public SubscriptionResponse getMySubscription(UUID userId) {
        UserCache user = getUserCache(userId);
        Subscription subscription = subscriptionRepository
                .findByUser(user)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "No subscription found for user : " + userId
                ));
        return toResponse(subscription);
    }

    // ════════════════════════════════════
    // PREVIEW CHANGE PLAN
    // display prorata before confirmation
    // I have to check if the renewalDate equal the date of upgrade !!!
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public ProrataResponse previewChangePlan(
            UUID userId,
            ChangePlanRequest request) {

        UserCache user = getUserCache(userId);
        Subscription subscription = getActiveSubscription(user);

        // if (LocalDate.now().isBefore(subscription.getNextRenewalDate())) {
        if (!stateMachine.canChangePlan(subscription.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Cannot change plan with status : "
                            + subscription.getStatus()
            );
        }

        Plan newPlan = getActivePlan(request.newPlanId());

        if (newPlan.getId().equals(subscription.getPlan().getId())) {
            throw new IllegalArgumentException(
                    "New plan is the same as current plan"
            );
        }

        return prorataCalculator.calculate(subscription, newPlan);

    }

    // ════════════════════════════════════
    // CONFIRM CHANGE PLAN
    // called after client comfirmation
    // ════════════════════════════════════

    @Transactional
    public SubscriptionResponse confirmChangePlan(
            UUID userId,
            ChangePlanRequest request) {

        UserCache user = getUserCache(userId);
        Subscription subscription = getActiveSubscription(user);
        Plan newPlan = getActivePlan(request.newPlanId());
        Plan previousPlan = subscription.getPlan();

        ProrataResponse prorata = prorataCalculator
                .calculate(subscription, newPlan);

        if ("UPGRADE".equals(prorata.changeType())) {
            return applyUpgrade(subscription, newPlan,
                    previousPlan, prorata);
        } else {
            return applyDowngrade(subscription, newPlan);
        }
    }

    // ════════════════════════════════════
    // CANCEL SUBSCRIPTION
    // ════════════════════════════════════

    @Transactional
    public SubscriptionResponse cancelSubscription(UUID userId) {
        UserCache user = getUserCache(userId);
        Subscription subscription = subscriptionRepository
                .findByUser(user)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "No subscription found"
                ));

        SubscriptionStatus previousStatus = subscription.getStatus();

        stateMachine.validateTransition(
                previousStatus,
                SubscriptionStatus.CANCELLED
        );

        // cancel planified downgrade if it exists
        subscription.setPendingPlanId(null);
        subscription.setPendingPlanEffectiveDate(null);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // saving event
        eventService.recordCancelled(subscription);

        // publish in Kafka
        eventPublisher.publishSubscriptionCancelled(subscription);

        return toResponse(subscription);
    }

    // ════════════════════════════════════
    // UPDATE STATUS (called by Kafka)
    // dunning-service publishes PaymentFailed
    // → subscription passe à PAST_DUE
    // ════════════════════════════════════

    @Transactional
    public void updateStatus(
            UUID subscriptionId,
            SubscriptionStatus newStatus,
            String note) {

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "Subscription not found : " + subscriptionId
                ));

        SubscriptionStatus previousStatus = subscription.getStatus();
        stateMachine.validateTransition(previousStatus, newStatus);

        subscription.setStatus(newStatus);
        subscriptionRepository.save(subscription);

        eventService.recordStatusChanged(
                subscription, previousStatus, newStatus, note
        );
    }

    // ════════════════════════════════════
    // APPLY PENDING DOWNGRADES (SCHEDULER)
    // appelé chaque nuit par billing-service
    // ════════════════════════════════════

    @Transactional
    public void applyPendingDowngrades() {
        List<Subscription> pending = subscriptionRepository
                .findPendingDowngrades(LocalDate.now());

        for (Subscription subscription : pending) {
            Plan newPlan = planRepository
                    .findById(subscription.getPendingPlanId())
                    .orElse(null);

            if (newPlan == null) continue;

            Plan previousPlan = subscription.getPlan();

            subscription.setPlan(newPlan);
            subscription.setPendingPlanId(null);
            subscription.setPendingPlanEffectiveDate(null);
            subscriptionRepository.save(subscription);

            eventService.record(
                    subscription,
                    SubscriptionEventType.DOWNGRADE_APPLIED,
                    previousPlan,
                    newPlan,
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.ACTIVE,
                    null,
                    "Downgrade applied"
            );
        }
    }

    // ════════════════════════════════════
    // ADMIN — GET ALL SUBSCRIPTIONS
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public List<AdminSubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    // ════════════════════════════════════
    // ADMIN — GET BY STATUS
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public List<AdminSubscriptionResponse> getByStatus(
            SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    // ════════════════════════════════════
    // ADMIN — DASHBOARD STATS
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long active = subscriptionRepository
                .countByStatus(SubscriptionStatus.ACTIVE);
        long pastDue = subscriptionRepository
                .countByStatus(SubscriptionStatus.PAST_DUE);
        long suspended = subscriptionRepository
                .countByStatus(SubscriptionStatus.SUSPENDED);
        long cancelled = subscriptionRepository
                .countByStatus(SubscriptionStatus.CANCELLED);

        return DashboardStatsResponse.builder()
                .activeCount(active)
                .pastDueCount(pastDue)
                .suspendedCount(suspended)
                .cancelledCount(cancelled)
                .totalCount(active + pastDue + suspended + cancelled)
                .build();
    }

    // ════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ════════════════════════════════════

    private SubscriptionResponse createNewSubscription(
            UserCache user,
            Plan plan) {

        LocalDate today = LocalDate.now();

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(today)
                .nextRenewalDate(today.plusMonths(1))
                .build();

        subscription = subscriptionRepository.save(subscription);
        eventService.recordSubscribed(subscription);
        eventPublisher.publishSubscriptionCreated(subscription);

        return toResponse(subscription);
    }

    private SubscriptionResponse resubscribe(
            Subscription existing,
            Plan plan) {

        LocalDate today = LocalDate.now();

        existing.setPlan(plan);
        existing.setStatus(SubscriptionStatus.ACTIVE);
        existing.setStartDate(today);
        existing.setNextRenewalDate(today.plusMonths(1));
        existing.setCancelledAt(null);
        existing.setPendingPlanId(null);
        existing.setPendingPlanEffectiveDate(null);

        existing = subscriptionRepository.save(existing);
        eventService.recordResubscribed(existing);
        eventPublisher.publishSubscriptionCreated(existing);

        return toResponse(existing);
    }

    private SubscriptionResponse applyUpgrade(
            Subscription subscription,
            Plan newPlan,
            Plan previousPlan,
            ProrataResponse prorata) {

        subscription.setPlan(newPlan);
        subscriptionRepository.save(subscription);

        eventService.recordUpgraded(
                subscription, previousPlan, prorata.prorataAmount()
        );

        eventPublisher.publishPlanChanged(subscription, prorata);

        return toResponse(subscription);
    }

    private SubscriptionResponse applyDowngrade(
            Subscription subscription,
            Plan newPlan) {

        subscription.setPendingPlanId(newPlan.getId());
        subscription.setPendingPlanEffectiveDate(
                subscription.getNextRenewalDate()
        );
        subscriptionRepository.save(subscription);

        eventService.recordDowngradeScheduled(subscription, newPlan);
        eventPublisher.publishDowngradeScheduled(subscription, newPlan);

        return toResponse(subscription);
    }

    private Subscription getActiveSubscription(UserCache user) {
        Subscription subscription = subscriptionRepository
                .findByUser(user)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "No subscription found"
                ));

        if (!stateMachine.isAccessAllowed(subscription.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Subscription is not active : "
                            + subscription.getStatus()
            );
        }

        return subscription;
    }

    private UserCache getUserCache(UUID userId) {
        return userCacheRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found in cache : " + userId
                ));
    }

    private Plan getActivePlan(UUID planId) {
        return planRepository.findByIdAndActiveTrue(planId)
                .orElseThrow(() -> new PlanNotFoundException(
                        "Plan not found or inactive : " + planId
                ));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .userId(s.getUser().getUserId())
                .firstName(s.getUser().getFirstName())
                .lastName(s.getUser().getLastName())
                .userEmail(s.getUser().getEmail())
                .planId(s.getPlan().getId())
                .planName(s.getPlan().getName())
                .planPrice(s.getPlan().getPrice())
                .status(s.getStatus())
                .startDate(s.getStartDate())
                .nextRenewalDate(s.getNextRenewalDate())
                .cancelledAt(s.getCancelledAt())
                .pendingPlanId(s.getPendingPlanId())
                .pendingPlanEffectiveDate(s.getPendingPlanEffectiveDate())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private AdminSubscriptionResponse toAdminResponse(Subscription s) {
        return AdminSubscriptionResponse.builder()
                .subscriptionId(s.getId())
                .userId(s.getUser().getUserId())
                .firstName(s.getUser().getFirstName())
                .lastName(s.getUser().getLastName())
                .userEmail(s.getUser().getEmail())
                .planName(s.getPlan().getName())
                .planPrice(s.getPlan().getPrice())
                .status(s.getStatus())
                .startDate(s.getStartDate())
                .nextRenewalDate(s.getNextRenewalDate())
                .cancelledAt(s.getCancelledAt())
                .pendingPlanEffectiveDate(s.getPendingPlanEffectiveDate())
                .build();
    }
}
