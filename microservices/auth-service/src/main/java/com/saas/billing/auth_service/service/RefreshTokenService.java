package com.saas.billing.auth_service.service;

import com.saas.billing.auth_service.config.JwtProperties;
import com.saas.billing.auth_service.domain.entity.RefreshToken;
import com.saas.billing.auth_service.domain.entity.User;
import com.saas.billing.auth_service.exception.TokenExpiredException;
import com.saas.billing.auth_service.exception.TokenInvalidException;
import com.saas.billing.auth_service.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtProperties jwtProperties, RefreshTokenRepository refreshTokenRepository) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // ════════════════════════════════════
    // CREATE A REFRESH TOKEN
    // ════════════════════════════════════
    @Transactional
    public String createRefreshToken(User user) {
        // Delete the old refresh token if it is existing
        if (refreshTokenRepository.existsByUser(user)) {
            refreshTokenRepository.deleteByUser(user);
        }

        // Generate a refresh token randomly
        String rawToken = UUID.randomUUID().toString() +
                UUID.randomUUID().toString();

        // Hash the token before storing it
        String hashedToken = hashToken(rawToken);

        // Calculate expiration date
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.refreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;

    }

    // ════════════════════════════════════
    // VALIDATE A REFRESH TOKEN
    // ════════════════════════════════════
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String hashedToken = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hashedToken)
                .orElseThrow(() -> new TokenInvalidException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            // Delete the expired token
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired");
        }

        return refreshToken;
    }

    // ════════════════════════════════════
    // DELETE TOKEN (LOGOUT)
    // ════════════════════════════════════
    @Transactional
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // ════════════════════════════════════
    // CLEAN UP EXPIRED TOKEN
    // ════════════════════════════════════
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteAllExpired();
    }

    // ════════════════════════════════════
    // PRIVATE METHOD — hashing token
    // ════════════════════════════════════

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing token", e);
        }
    }
}
