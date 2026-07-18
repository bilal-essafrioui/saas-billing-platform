package com.saas.billing.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    // ════════════════════════════════════
    // CRÉER UN STRIPE CUSTOMER
    // appelé lors du premier paiement
    // un customer par user
    // ════════════════════════════════════

    public String createCustomer(
            String email,
            String userId) throws StripeException {

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .putMetadata("userId", userId)
                .build();

        Customer customer = Customer.create(params);
        return customer.getId();
    }

    // ════════════════════════════════════
    // CRÉER UN PAYMENT INTENT
    // appelé pour INITIAL_SUBSCRIPTION
    // retourne clientSecret pour le frontend
    // ════════════════════════════════════

    public PaymentIntent createPaymentIntent(
            BigDecimal amount,
            String currency,
            String stripeCustomerId,
            String idempotencyKey) throws StripeException {

        // convertir en centimes
        long amountInCents = amount
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency(currency.toLowerCase())
                        .setCustomer(stripeCustomerId)
                        .setSetupFutureUsage(
                                // sauvegarder la carte pour les renouvellements
                                PaymentIntentCreateParams
                                        .SetupFutureUsage.OFF_SESSION
                        )
                        .build();

        com.stripe.net.RequestOptions options =
                com.stripe.net.RequestOptions.builder()
                        .setIdempotencyKey(idempotencyKey)
                        .build();

        return PaymentIntent.create(params, options);
    }

    // ════════════════════════════════════
    // CRÉER UN PAYMENT INTENT OFF-SESSION
    // appelé pour RENEWAL et RETRY
    // pas besoin du frontend
    // Stripe débite automatiquement la carte sauvegardée
    // ════════════════════════════════════

    public PaymentIntent createOffSessionPaymentIntent(
            BigDecimal amount,
            String currency,
            String stripeCustomerId,
            String stripePaymentMethodId,
            String idempotencyKey) throws StripeException {

        long amountInCents = amount
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency(currency.toLowerCase())
                        .setCustomer(stripeCustomerId)
                        .setPaymentMethod(stripePaymentMethodId)
                        .setConfirm(true)
                        // off_session = pas d'action user requise
                        .setOffSession(true)
                        .build();

        com.stripe.net.RequestOptions options =
                com.stripe.net.RequestOptions.builder()
                        .setIdempotencyKey(idempotencyKey)
                        .build();

        return PaymentIntent.create(params, options);
    }
}