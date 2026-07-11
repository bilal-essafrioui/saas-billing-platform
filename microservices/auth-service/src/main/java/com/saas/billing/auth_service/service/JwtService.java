package com.saas.billing.auth_service.service;

import com.saas.billing.auth_service.config.JwtProperties;
import com.saas.billing.auth_service.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8)
        );
    }

    // ════════════════════════════════════
    // TOKEN GENERATION
    // ════════════════════════════════════

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration()))
                .signWith(signingKey)
                .compact();
    }

    // ════════════════════════════════════
    //  TOKEN VALIDATION
    // ════════════════════════════════════
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ════════════════════════════════════
    // DATA EXTRACTION
    // ════════════════════════════════════
    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public long getExpiration() {
        //return getClaims(token).getExpiration();
        return jwtProperties.accessTokenExpiration();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
