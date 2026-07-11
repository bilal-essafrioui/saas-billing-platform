package com.saas.billing.auth_service.repository;

import com.saas.billing.auth_service.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // find user by email
    Optional<User> findByEmail(String email);

    // verify if an email exists
    boolean existsByEmail(String email);

    // update date of the last login
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginAt(
            @Param("userId") UUID userId,
            @Param("lastLoginAt") LocalDateTime lastLoginAt
    );
}