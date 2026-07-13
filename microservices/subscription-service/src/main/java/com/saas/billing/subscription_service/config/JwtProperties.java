package com.saas.billing.subscription_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        String secretKey,

        long accessTokenExpiration

) {}