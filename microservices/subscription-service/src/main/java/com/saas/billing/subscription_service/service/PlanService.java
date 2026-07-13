package com.saas.billing.subscription_service.service;

import com.saas.billing.subscription_service.domain.entity.Plan;
import com.saas.billing.subscription_service.dto.request.CreatePlanRequest;
import com.saas.billing.subscription_service.dto.response.PlanResponse;
import com.saas.billing.subscription_service.exception.PlanNotFoundException;
import com.saas.billing.subscription_service.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PlanService {
    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    // ════════════════════════════════════
    // GET ALL ACTIVE PLANS
    // public → visible par tous
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllActivePlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ════════════════════════════════════
    // GET PLAN BY ID
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public PlanResponse getPlanById(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(
                        "Plan not found : " + planId
                ));
        return toResponse(plan);
    }

    // ════════════════════════════════════
    // CREATE PLAN (ADMIN ONLY)
    // ════════════════════════════════════

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .active(request.active())
                .build();

        plan = planRepository.save(plan);
        return toResponse(plan);
    }

    // ════════════════════════════════════
    // DEACTIVATE PLAN (ADMIN ONLY)
    // no delete → just deactivate
    // Existing subscriptions they are not affected
    // ════════════════════════════════════

    @Transactional
    public PlanResponse deactivatePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(
                        "Plan not found : " + planId
                ));

        plan.setActive(false);
        plan = planRepository.save(plan);
        return toResponse(plan);
    }

    // ════════════════════════════════════
    // GET PLAN ENTITY (internal use)
    // used by other services
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public Plan getPlanEntityById(UUID planId) {
        return planRepository.findByIdAndActiveTrue(planId)
                .orElseThrow(() -> new PlanNotFoundException(
                        "Plan not found or inactive : " + planId
                ));
    }

    // ════════════════════════════════════
    // CONVERSION ENTITY → DTO
    // ════════════════════════════════════

    private PlanResponse toResponse(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .description(plan.getDescription())
                .active(plan.isActive())
                .build();
    }
}
