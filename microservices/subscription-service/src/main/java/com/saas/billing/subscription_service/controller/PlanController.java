package com.saas.billing.subscription_service.controller;

import com.saas.billing.subscription_service.dto.request.CreatePlanRequest;
import com.saas.billing.subscription_service.dto.response.PlanResponse;
import com.saas.billing.subscription_service.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/plans
    // public → tout le monde peut voir les plans
    // ════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    // ════════════════════════════════════
    // GET /api/subscriptions/plans/{id}
    // public
    // ════════════════════════════════════

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlanById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    // ════════════════════════════════════
    // POST /api/subscriptions/plans
    // ADMIN ONLY
    // ════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> createPlan(
            @Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(planService.createPlan(request));
    }

    // ════════════════════════════════════
    // DELETE /api/subscriptions/plans/{id}
    // ADMIN ONLY
    // désactive le plan (pas de suppression)
    // ════════════════════════════════════

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> deactivatePlan(
            @PathVariable UUID id) {
        return ResponseEntity.ok(planService.deactivatePlan(id));
    }
}