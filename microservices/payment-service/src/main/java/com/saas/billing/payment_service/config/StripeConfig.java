package com.saas.billing.payment_service.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api-key}")
    private String apiKey;

    // initialiser Stripe au démarrage
    // toutes les classes Stripe utilisent
    // cette clé automatiquement
    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }
}