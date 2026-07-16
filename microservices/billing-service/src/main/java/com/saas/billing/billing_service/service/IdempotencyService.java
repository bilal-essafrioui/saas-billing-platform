package com.saas.billing.billing_service.service;

import com.saas.billing.billing_service.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final InvoiceRepository invoiceRepository;

    public IdempotencyService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // ════════════════════════════════════
    // GÉNÉRER LA CLÉ D'IDEMPOTENCE
    // format : subscriptionId_YYYY-MM
    // ex : "uuid-123_2026-08"
    // ════════════════════════════════════

    public String generateKey(UUID subscriptionId, LocalDate date) {
        return subscriptionId.toString()
                + "_"
                + date.getYear()
                + "-"
                + String.format("%02d", date.getMonthValue());
    }

    // ════════════════════════════════════
    // GÉNÉRER CLÉ POUR PRORATION
    // format : subscriptionId_PRORATION_YYYY-MM-DD
    // ════════════════════════════════════

    public String generateProrataKey(
            UUID subscriptionId,
            LocalDate date) {
        return subscriptionId.toString()
                + "_PRORATION_"
                + date.toString();
    }

    // ════════════════════════════════════
    // VÉRIFIER SI FACTURE DÉJÀ EXISTANTE
    // ════════════════════════════════════

    public boolean invoiceAlreadyExists(String idempotencyKey) {
        return invoiceRepository.existsByIdempotencyKey(idempotencyKey);
    }
}