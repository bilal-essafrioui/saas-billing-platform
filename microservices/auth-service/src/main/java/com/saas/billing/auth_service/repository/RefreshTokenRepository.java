package com.saas.billing.auth_service.repository;

import com.saas.billing.auth_service.domain.entity.RefreshToken;
import com.saas.billing.auth_service.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // find refresh token by its hash
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // find refresh token of a specific user
    Optional<RefreshToken> findByUser(User user);

    /*
        delete a refresh token of a user
        called in logout
    */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);

    // verify if a user already have a refresh token
    boolean existsByUser(User user);


    // delete expired refresh token
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    void deleteAllExpired();
}