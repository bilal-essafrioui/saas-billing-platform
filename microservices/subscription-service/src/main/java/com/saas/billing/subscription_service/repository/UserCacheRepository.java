package com.saas.billing.subscription_service.repository;

import com.saas.billing.subscription_service.domain.entity.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, UUID> {

    Optional<UserCache> findByEmail(String email);

    boolean existsByEmail(String email);
}
