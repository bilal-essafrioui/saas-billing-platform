package com.saas.billing.payment_service.controller;

import com.saas.billing.payment_service.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ════════════════════════════════════
    // POST /api/webhooks/stripe
    // endpoint public
    // Stripe envoie le résultat du paiement ici
    // ════════════════════════════════════

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        // vérifier la signature Stripe
        // empêche les fausses requêtes
        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, webhookSecret
            );
            System.out.println("=================================");
            System.out.println("EVENT = " + event.getType());
            System.out.println("=================================");
        } catch (SignatureVerificationException e) {
            System.err.println(
                    "Invalid Stripe webhook signature"
            );
            return ResponseEntity.badRequest()
                    .body("Invalid signature");
        }

        // traiter selon le type d'event
        switch (event.getType()) {

            case "payment_intent.succeeded" -> {
                Optional<StripeObject> stripeObject =
                        event.getDataObjectDeserializer()
                                .getObject();
                System.out.println("Stripe object present = " + stripeObject.isPresent());
                if (stripeObject.isPresent()) {
                    System.out.println("Entered payment_intent.succeeded");
                    PaymentIntent paymentIntent =
                            (PaymentIntent) stripeObject.get();

                    System.out.println("Intent ID = " + paymentIntent.getId());
                    System.out.println("Customer = " + paymentIntent.getCustomer());
                    System.out.println("Payment Method = " + paymentIntent.getPaymentMethod());

                    // extraire les infos de la carte
                    String pmId = paymentIntent
                            .getPaymentMethod();
                    String customerId = paymentIntent
                            .getCustomer();

                    // récupérer les détails de la carte
                    // via l'API Stripe
                    handleSucceededPayment(
                            paymentIntent.getId(),
                            customerId,
                            pmId
                    );
                }
            }

            case "payment_intent.payment_failed" -> {
                Optional<StripeObject> stripeObject =
                        event.getDataObjectDeserializer()
                                .getObject();

                if (stripeObject.isPresent()) {
                    PaymentIntent paymentIntent =
                            (PaymentIntent) stripeObject.get();

                    String failureReason = "Payment failed";
                    if (paymentIntent.getLastPaymentError() != null) {
                        failureReason = paymentIntent
                                .getLastPaymentError()
                                .getMessage();
                    }

                    paymentService.handlePaymentFailed(
                            paymentIntent.getId(),
                            failureReason
                    );
                }
            }

            case "setup_intent.succeeded" -> {

                Optional<StripeObject> stripeObject =
                        event.getDataObjectDeserializer()
                                .getObject();

                if (stripeObject.isPresent()) {

                    SetupIntent setupIntent =
                            (SetupIntent) stripeObject.get();

                    handleSucceededSetupIntent(
                            setupIntent.getId()
                    );
                }
            }

            default -> System.out.println(
                    "Unhandled event type : " + event.getType()
            );
        }

        return ResponseEntity.ok("Event received");
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE
    // récupérer les détails de la carte Stripe
    // ════════════════════════════════════

    private void handleSucceededPayment(
            String paymentIntentId,
            String customerId,
            String paymentMethodId) {

        try {
            System.out.println("===== PAYMENT SUCCEEDED WEBHOOK =====");
            System.out.println("PaymentIntentId : " + paymentIntentId);
            System.out.println("CustomerId      : " + customerId);
            System.out.println("PaymentMethodId : " + paymentMethodId);

            com.stripe.model.PaymentMethod pm =
                    com.stripe.model.PaymentMethod.retrieve(
                            paymentMethodId
                    );

            System.out.println("Stripe PaymentMethod retrieved.");

            com.stripe.model.PaymentMethod.Card card =
                    pm.getCard();

            paymentService.handlePaymentSucceeded(
                    paymentIntentId,
                    customerId,
                    paymentMethodId,
                    card != null ? card.getBrand() : null,
                    card != null ? card.getLast4() : null,
                    card != null ? card.getExpMonth().intValue() : null,
                    card != null ? card.getExpYear().intValue() : null
            );

            System.out.println("Payment service finished successfully.");

        } catch (Exception e) {
            /*System.err.println(
                    "Error handling succeeded payment : "
                            + e.getMessage()
            );*/
            e.printStackTrace();
        }
    }

    private void handleSucceededSetupIntent(
            String setupIntentId) {

        try {

            System.out.println("===== SETUP INTENT SUCCEEDED =====");

            SetupIntent setupIntent =
                    SetupIntent.retrieve(setupIntentId);

            String customerId =
                    setupIntent.getCustomer();

            String paymentMethodId =
                    setupIntent.getPaymentMethod();

            System.out.println("CustomerId      : " + customerId);
            System.out.println("PaymentMethodId : " + paymentMethodId);

            com.stripe.model.PaymentMethod paymentMethod =
                    com.stripe.model.PaymentMethod.retrieve(
                            paymentMethodId
                    );

            com.stripe.model.PaymentMethod.Card card =
                    paymentMethod.getCard();

            paymentService.handleSetupIntentSucceeded(
                    customerId,
                    paymentMethodId,
                    card != null ? card.getBrand() : null,
                    card != null ? card.getLast4() : null,
                    card != null ? card.getExpMonth().intValue() : null,
                    card != null ? card.getExpYear().intValue() : null
            );

            System.out.println("SetupIntent handled successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}