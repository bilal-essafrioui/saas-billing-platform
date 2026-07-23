package com.saas.billing.payment_service.controller;

import com.saas.billing.payment_service.dto.request.ChargePaymentRequest;
import com.saas.billing.payment_service.dto.request.CreatePaymentIntentRequest;
import com.saas.billing.payment_service.dto.response.ChargePaymentResponse;
import com.saas.billing.payment_service.dto.response.CreatePaymentIntentResponse;
import com.saas.billing.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/payments")
public class InternalPaymentController {

    private final PaymentService paymentService;

    public InternalPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ════════════════════════════════════
    // POST /internal/payments/create-intent
    // appelé par subscription-service (HTTP)
    // PAS exposé via API Gateway
    // ════════════════════════════════════

    @PostMapping("/create-intent")
    public ResponseEntity<CreatePaymentIntentResponse> createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") String userEmail
    ) {

        try {
            CreatePaymentIntentResponse response =
                    paymentService.createPaymentIntent(
                            request, userEmail
                    );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    @PostMapping("/charge")
    public ResponseEntity<ChargePaymentResponse> chargePayment(
            @Valid @RequestBody ChargePaymentRequest request
    ) {
        System.out.println("6. Payment controller reached");
        ChargePaymentResponse response =
                paymentService.chargePayment(request);

        return ResponseEntity.ok(response);
    }
}
