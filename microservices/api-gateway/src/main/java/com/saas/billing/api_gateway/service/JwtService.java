package com.saas.billing.api_gateway.service;

import com.saas.billing.api_gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

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

