package com.saas.billing.api_gateway.filter;

import com.saas.billing.api_gateway.service.JwtService;
import com.saas.billing.api_gateway.service.TokenCookieService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthFilter extends
        AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtService jwtService;
    private final TokenCookieService tokenCookieService;

    public JwtAuthFilter(
            JwtService jwtService,
            TokenCookieService tokenCookieService) {
        super(Config.class);
        this.jwtService = jwtService;
        this.tokenCookieService = tokenCookieService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // extraire JWT depuis le cookie
            String token = tokenCookieService
                    .extractJwtFromCookie(exchange.getRequest());

            // pas de token → 401
            if (token == null) {
                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (!jwtService.isTokenValid(token)) {
                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // extraire les infos du JWT
            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            String email = jwtService.extractEmail(token);

            // ajouter le JWT dans le header Authorization
            // pour les microservices en aval
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("Authorization", "Bearer " + token)
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build()
            );
        };
    }

    public static class Config {}
}
