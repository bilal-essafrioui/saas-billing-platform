package com.saas.billing.subscription_service.controller;

import com.saas.billing.subscription_service.domain.enums.SubscriptionStatus;
import com.saas.billing.subscription_service.dto.request.ChangePlanRequest;
import com.saas.billing.subscription_service.dto.request.SubscribeRequest;
import com.saas.billing.subscription_service.dto.response.AdminSubscriptionResponse;
import com.saas.billing.subscription_service.dto.response.DashboardStatsResponse;
import com.saas.billing.subscription_service.dto.response.ProrataResponse;
import com.saas.billing.subscription_service.dto.response.SubscriptionEventResponse;
import com.saas.billing.subscription_service.dto.response.SubscriptionResponse;
import com.saas.billing.subscription_service.security.AuthenticatedUser;
import com.saas.billing.subscription_service.service.SubscriptionEventService;
import com.saas.billing.subscription_service.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionEventService eventService;

    public SubscriptionController(
            SubscriptionService subscriptionService,
            SubscriptionEventService eventService) {
        this.subscriptionService = subscriptionService;
        this.eventService = eventService;
    }

    // ════════════════════════════════════
    // POST /api/subscriptions/subscribe
    // CUSTOMER
    // créer ou resubscribe
    // ════════════════════════════════════

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request) {

        UUID userId = extractUserId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subscriptionService.subscribe(userId, request));
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/me
    // CUSTOMER
    // voir son abonnement
    // ════════════════════════════════════

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SubscriptionResponse> getMySubscription() {
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                subscriptionService.getMySubscription(userId)
        );
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/me/preview-change
    // CUSTOMER
    // voir le prorata avant de confirmer
    // ════════════════════════════════════

    @PostMapping("/me/preview-change")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ProrataResponse> previewChangePlan(
            @Valid @RequestBody ChangePlanRequest request) {

        UUID userId = extractUserId();
        return ResponseEntity.ok(
                subscriptionService.previewChangePlan(userId, request)
        );
    }

    // ════════════════════════════════════
    // POST /api/subscriptions/me/change-plan
    // CUSTOMER
    // confirmer le changement de plan
    // ════════════════════════════════════

    @PostMapping("/me/change-plan")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SubscriptionResponse> changePlan(
            @Valid @RequestBody ChangePlanRequest request) {

        UUID userId = extractUserId();
        return ResponseEntity.ok(
                subscriptionService.confirmChangePlan(userId, request)
        );
    }

    // ════════════════════════════════════
    // DELETE /api/subscriptions/me/cancel
    // CUSTOMER
    // résilier l'abonnement
    // ════════════════════════════════════

    @DeleteMapping("/me/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SubscriptionResponse> cancelSubscription() {
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                subscriptionService.cancelSubscription(userId)
        );
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/me/history
    // CUSTOMER
    // voir l'historique des changements
    // ════════════════════════════════════

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<SubscriptionEventResponse>> getMyHistory() {
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                eventService.getHistoryByUserId(userId)
        );
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/admin/all
    // ADMIN
    // voir toutes les subscriptions
    // ════════════════════════════════════

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminSubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(
                subscriptionService.getAllSubscriptions()
        );
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/admin/by-status
    // ADMIN
    // filtrer par statut
    // ════════════════════════════════════

    @GetMapping("/admin/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminSubscriptionResponse>> getByStatus(
            @RequestParam SubscriptionStatus status) {
        return ResponseEntity.ok(
                subscriptionService.getByStatus(status)
        );
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/admin/stats
    // ADMIN
    // stats dashboard
    // ════════════════════════════════════

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(
                subscriptionService.getDashboardStats()
        );
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/admin/user/{userId}/history
    // ADMIN
    // voir l'historique d'un user spécifique
    // ════════════════════════════════════

    @GetMapping("/admin/user/{userId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionEventResponse>> getUserHistory(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(
                eventService.getHistoryByUserId(userId)
        );
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE
    // extraire userId depuis SecurityContext
    // mis par JwtAuthenticationFilter
    // ════════════════════════════════════

    private UUID extractUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return UUID.fromString(user.userId());
    }
}