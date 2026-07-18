package com.saas.billing.payment_service.controller;

import com.saas.billing.payment_service.dto.response.PaymentMethodResponse;
import com.saas.billing.payment_service.dto.response.PaymentResponse;
import com.saas.billing.payment_service.service.PaymentMethodService;
import com.saas.billing.payment_service.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;

    public PaymentController(
            PaymentService paymentService,
            PaymentMethodService paymentMethodService) {
        this.paymentService = paymentService;
        this.paymentMethodService = paymentMethodService;
    }

    // ════════════════════════════════════
    // GET /api/payments/me
    // CUSTOMER → son historique
    // ════════════════════════════════════

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                paymentService.getMyPayments(userId)
        );
    }

    // ════════════════════════════════════
    // GET /api/payments/method
    // CUSTOMER → sa carte enregistrée
    // ════════════════════════════════════

    @GetMapping("/method")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentMethodResponse> getMyPaymentMethod() {
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                paymentMethodService.getMyPaymentMethod(userId)
        );
    }

    // ════════════════════════════════════
    // GET /api/payments/admin/all
    // ADMIN → tous les paiements
    // ════════════════════════════════════

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // ════════════════════════════════════
    // GET /api/payments/admin/failed
    // ADMIN → paiements échoués
    // ════════════════════════════════════

    @GetMapping("/admin/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getFailedPayments() {
        return ResponseEntity.ok(paymentService.getFailedPayments());
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE
    // ════════════════════════════════════

    private UUID extractUserId() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        return UUID.fromString((String) auth.getDetails());
    }
}
