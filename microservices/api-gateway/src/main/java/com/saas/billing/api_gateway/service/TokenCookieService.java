package com.saas.billing.api_gateway.service;

import com.saas.billing.api_gateway.config.JwtProperties;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;

@Service
public class TokenCookieService {

    private final JwtProperties jwtProperties;

    private static final boolean SECURE = false;

    public TokenCookieService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // ════════════════════════════════════
    // EXTRAIRE JWT DU COOKIE
    // ════════════════════════════════════

    public String extractJwtFromCookie(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies()
                .getFirst("jwt_token");
        return cookie != null ? cookie.getValue() : null;
    }

    // ════════════════════════════════════
    // EXTRAIRE REFRESH TOKEN DU COOKIE
    // ════════════════════════════════════

    public String extractRefreshTokenFromCookie(
            ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies()
                .getFirst("refresh_token");
        return cookie != null ? cookie.getValue() : null;
    }

    // ════════════════════════════════════
    // AJOUTER JWT COOKIE
    // ════════════════════════════════════

    public void addJwtCookie(
            ServerHttpResponse response,
            String token) {
        ResponseCookie cookie = ResponseCookie
                .from("jwt_token", token)
                .httpOnly(true)
                .secure(SECURE)
                .path("/")
                .maxAge(jwtProperties.accessTokenExpiration() / 1000)
                .sameSite("Strict")
                .build();
        response.addCookie(cookie);
    }

    // ════════════════════════════════════
    // AJOUTER REFRESH TOKEN COOKIE
    // ════════════════════════════════════

    public void addRefreshCookie(
            ServerHttpResponse response,
            String token) {
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", token)
                .httpOnly(true)
                .secure(SECURE)
                .path("/api/auth/refresh")
                .maxAge(jwtProperties.refreshTokenExpiration() / 1000)
                .sameSite("Strict")
                .build();
        response.addCookie(cookie);
    }

    // ════════════════════════════════════
    // SUPPRIMER LES COOKIES (LOGOUT)
    // ════════════════════════════════════

    public void clearCookies(ServerHttpResponse response) {
        ResponseCookie jwtCookie = ResponseCookie
                .from("jwt_token", "")
                .httpOnly(true)
                .secure(SECURE)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(SECURE)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addCookie(jwtCookie);
        response.addCookie(refreshCookie);
    }
}
