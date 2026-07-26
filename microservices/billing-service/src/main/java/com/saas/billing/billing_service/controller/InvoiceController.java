package com.saas.billing.billing_service.controller;

import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.PeriodValue;
import com.saas.billing.billing_service.dto.request.FilterMyInvoicesRequest;
import com.saas.billing.billing_service.dto.response.InvoiceResponse;
import com.saas.billing.billing_service.security.AuthenticatedUser;
import com.saas.billing.billing_service.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // ════════════════════════════════════
    // GET /api/invoices/me
    // CUSTOMER → ses propres factures
    // ════════════════════════════════════

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices() {

        UUID userId = extractUserId();

        return ResponseEntity.ok(
                invoiceService.getMyInvoices(userId)
        );
    }

    @GetMapping("/me/filter")
    public ResponseEntity<List<InvoiceResponse>> filterMyInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam PeriodValue period

    ){
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                invoiceService.filterMyInvoices(userId, status, period)
        );
    }

    // ════════════════════════════════════
    // GET /api/invoices/{id}
    // CUSTOMER → une facture spécifique
    // ════════════════════════════════════

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable UUID id) {
        UUID userId = extractUserId();
        return ResponseEntity.ok(
                invoiceService.getInvoiceById(id, userId)
        );
    }

    // ════════════════════════════════════
    // GET /api/invoices/admin/all
    // ADMIN → toutes les factures
    // ════════════════════════════════════

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    // ════════════════════════════════════
    // MÉTHODE PRIVÉE
    // ════════════════════════════════════

    private UUID extractUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return UUID.fromString(user.userId());
    }
}
