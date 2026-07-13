package com.saas.billing.subscription_service.repository;

import com.saas.billing.subscription_service.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    // GET ALL ACTIVE PLANS
    // Displayed to user in choice page
    List<Plan> findByActiveTrue();

    // Verify if a plan exists and active
    Optional<Plan> findByIdAndActiveTrue(UUID id);

    // Verify if a plan exists by name
    boolean existsByName(String name);
}
