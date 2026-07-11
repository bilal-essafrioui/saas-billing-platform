package com.saas.billing.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        String secretKey,

        long accessTokenExpiration,

        long refreshTokenExpiration

) {}
