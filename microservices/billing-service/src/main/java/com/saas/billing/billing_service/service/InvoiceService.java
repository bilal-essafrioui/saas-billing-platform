package com.saas.billing.billing_service.service;

import com.saas.billing.billing_service.domain.entity.Invoice;
import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.InvoiceType;
import com.saas.billing.billing_service.domain.enums.PeriodValue;
import com.saas.billing.billing_service.dto.request.FilterMyInvoicesRequest;
import com.saas.billing.billing_service.dto.response.InvoiceResponse;
import com.saas.billing.billing_service.exception.InvoiceNotFoundException;
import com.saas.billing.billing_service.messaging.producer.BillingEventPublisher;
import com.saas.billing.billing_service.messaging.producer.ProrationInvoiceEventPublisher;
import com.saas.billing.billing_service.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final IdempotencyService idempotencyService;
    private final BillingEventPublisher eventPublisher;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            IdempotencyService idempotencyService,
            BillingEventPublisher eventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.idempotencyService = idempotencyService;
        this.eventPublisher = eventPublisher;
    }

    // create first invoice

    // ════════════════════════════════════
    // CRÉER FACTURE MENSUELLE (RECURRING)
    // appelé par le scheduler
    // ════════════════════════════════════

    @Transactional
    public Invoice createRecurringInvoice(
            UUID subscriptionId,
            UUID userId,
            String userEmail,
            UUID planId,
            BigDecimal amount,
            LocalDate billingDate) {

        String idempotencyKey = idempotencyService
                .generateKey(subscriptionId, billingDate);

        // vérifier idempotence
        if (idempotencyService.invoiceAlreadyExists(idempotencyKey)) {
            // invoice already exists → return the existing one
            return invoiceRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow();
        }

        Invoice invoice = Invoice.builder()
                .subscriptionId(subscriptionId)
                .userId(userId)
                .userEmail(userEmail)
                .amount(amount)
                .status(InvoiceStatus.PENDING)
                .type(InvoiceType.RECURRING)
                .idempotencyKey(idempotencyKey)
                .billingPeriodStart(billingDate)
                .billingPeriodEnd(billingDate.plusMonths(1).minusDays(1))
                .build();

        invoice = invoiceRepository.save(invoice);

        // publish to Kafka → payment-service va déclencher le paiement
        eventPublisher.publishInvoiceGenerated(invoice, planId);

        return invoice;
    }

    // create first invoice
    @Transactional
    public Invoice createFirstInvoiceAfterPayment(
            UUID subscriptionId,
            UUID paymentId,
            UUID userId,
            String userEmail,
            BigDecimal amount,
            LocalDate billingDate
    ) {

        String idempotencyKey = idempotencyService
                .generateKey(subscriptionId, billingDate);

        if (idempotencyService.invoiceAlreadyExists(idempotencyKey)) {
            return invoiceRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow();
        }

        Invoice invoice = Invoice.builder()
                .subscriptionId(subscriptionId)
                .userId(userId)
                .userEmail(userEmail)
                .amount(amount)
                .status(InvoiceStatus.PAID)   // ← already paid
                .paidAt(LocalDateTime.now())  // ← payment already happened
                .type(InvoiceType.RECURRING)
                .idempotencyKey(idempotencyKey)
                .billingPeriodStart(billingDate)
                .billingPeriodEnd(billingDate.plusMonths(1).minusDays(1))
                .build();

        invoice = invoiceRepository.save(invoice);

        eventPublisher.publishFirstInvoiceCreated(
                paymentId,
                invoice
        );

        return invoice;
    }

    // ════════════════════════════════════
    // CRÉER FACTURE PRORATA (UPGRADE)
    // appelé quand un client upgrade son plan
    // ════════════════════════════════════

    @Transactional
    public Invoice createProrataInvoice(
            UUID subscriptionId,
            UUID userId,
            String userEmail,
            BigDecimal amount,
            LocalDate upgradeDate,
            LocalDate periodEnd) {

        String idempotencyKey = idempotencyService
                .generateProrataKey(subscriptionId, upgradeDate);

        // vérifier idempotence
        if (idempotencyService.invoiceAlreadyExists(idempotencyKey)) {
            return invoiceRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow();
        }

        Invoice invoice = Invoice.builder()
                .subscriptionId(subscriptionId)
                .userId(userId)
                .userEmail(userEmail)
                .amount(amount)
                .status(InvoiceStatus.PAID)
                .paidAt(LocalDateTime.now())
                .type(InvoiceType.PRORATION)
                .idempotencyKey(idempotencyKey)
                .billingPeriodStart(upgradeDate)
                .billingPeriodEnd(periodEnd)
                .build();

        invoice = invoiceRepository.save(invoice);


        return invoice;
    }

    // ════════════════════════════════════
    // METTRE À JOUR STATUT
    // called by PaymentEventConsumer(here)
    // ════════════════════════════════════

    @Transactional
    public void markAsPaid(UUID invoiceId) {
        Invoice invoice = getById(invoiceId);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void markAsFailed(UUID invoiceId) {
        Invoice invoice = getById(invoiceId);
        invoice.setStatus(InvoiceStatus.FAILED);
        invoiceRepository.save(invoice);
    }

    // ════════════════════════════════════
    // LECTURE
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getMyInvoices(UUID userId) {
        return invoiceRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> filterMyInvoices(
            UUID userId,
            InvoiceStatus status,
            PeriodValue period
    ) {

        LocalDate startDate = null;

        if (period != PeriodValue.ALL_TIME) {
            startDate = LocalDate.now()
                    .minusMonths(period.getMonths());
        }

        return invoiceRepository
                .filterInvoices(
                        userId,
                        status,
                        startDate
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID invoiceId, UUID userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found : " + invoiceId
                ));

        // vérifier que la facture appartient au user
        if (!invoice.getUserId().equals(userId)) {
            throw new InvoiceNotFoundException(
                    "Invoice not found : " + invoiceId
            );
        }

        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ════════════════════════════════════

    private Invoice getById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found : " + invoiceId
                ));
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .subscriptionId(invoice.getSubscriptionId())
                .userId(invoice.getUserId())
                .userEmail(invoice.getUserEmail())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .type(invoice.getType())
                .idempotencyKey(invoice.getIdempotencyKey())
                .billingPeriodStart(invoice.getBillingPeriodStart())
                .billingPeriodEnd(invoice.getBillingPeriodEnd())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}