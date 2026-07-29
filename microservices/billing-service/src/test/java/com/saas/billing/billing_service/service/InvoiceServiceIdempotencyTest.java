package com.saas.billing.billing_service.service;

import com.saas.billing.billing_service.domain.entity.Invoice;
import com.saas.billing.billing_service.domain.enums.InvoiceStatus;
import com.saas.billing.billing_service.domain.enums.InvoiceType;
import com.saas.billing.billing_service.messaging.producer.BillingEventPublisher;
import com.saas.billing.billing_service.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService Idempotency Tests")
class InvoiceServiceIdempotencyTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private BillingEventPublisher eventPublisher;

    @InjectMocks
    private InvoiceService invoiceService;

    private UUID subscriptionId;
    private UUID userId;
    private Invoice existingInvoice;
    private UUID planId;

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();

        existingInvoice = Invoice.builder()
                .id(UUID.randomUUID())
                .subscriptionId(subscriptionId)
                .userId(userId)
                .userEmail("ahmed@email.com")
                .amount(new BigDecimal("30.00"))
                .status(InvoiceStatus.PENDING)
                .type(InvoiceType.RECURRING)
                .idempotencyKey(
                        subscriptionId + "_2026-08"
                )
                .billingPeriodStart(LocalDate.of(2026, 8, 1))
                .billingPeriodEnd(LocalDate.of(2026, 8, 31))
                .build();
    }

    @Test
    @DisplayName("Créer facture → facture sauvegardée + event publié")
    void createRecurringInvoice_shouldSaveAndPublish() {
        // GIVEN
        String key = subscriptionId + "_2026-08";
        when(idempotencyService.generateKey(any(), any()))
                .thenReturn(key);
        when(idempotencyService.invoiceAlreadyExists(key))
                .thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(existingInvoice);

        // WHEN
        Invoice result = invoiceService.createRecurringInvoice(
                subscriptionId,
                userId,
                "ahmed@email.com",
                planId,
                new BigDecimal("30.00"),
                LocalDate.of(2026, 8, 1)
        );

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getAmount())
                .isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(result.getStatus())
                .isEqualTo(InvoiceStatus.PENDING);

        // facture sauvegardée
        verify(invoiceRepository).save(any(Invoice.class));

        // event publié
        verify(eventPublisher).publishInvoiceGenerated(any(Invoice.class), any(UUID.class));
    }

    @Test
    @DisplayName("IDEMPOTENCE : créer 2x même facture → 1 seule en base")
    void createRecurringInvoice_calledTwice_shouldNotCreateDuplicate() {
        // GIVEN
        String key = subscriptionId + "_2026-08";
        when(idempotencyService.generateKey(any(), any()))
                .thenReturn(key);

        // premier appel → pas de doublon
        when(idempotencyService.invoiceAlreadyExists(key))
                .thenReturn(false)   // 1er appel → créer
                .thenReturn(true);   // 2ème appel → ignorer

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(existingInvoice);
        when(invoiceRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingInvoice));

        // WHEN — appeler 2 fois avec les mêmes données
        Invoice first = invoiceService.createRecurringInvoice(
                subscriptionId, userId, "ahmed@email.com",planId,
                new BigDecimal("30.00"), LocalDate.of(2026, 8, 1)
        );
        Invoice second = invoiceService.createRecurringInvoice(
                subscriptionId, userId, "ahmed@email.com",planId,
                new BigDecimal("30.00"), LocalDate.of(2026, 8, 1)
        );

        // THEN
        // save appelé UNE SEULE FOIS
        verify(invoiceRepository, times(1)).save(any());

        // event publié UNE SEULE FOIS
        verify(eventPublisher, times(1))
                .publishInvoiceGenerated(any(Invoice.class), any(UUID.class));

        // les deux appels retournent la même facture
        assertThat(first.getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("IDEMPOTENCE : facture existante → retourne l'existante sans save")
    void createRecurringInvoice_whenExists_shouldReturnExisting() {
        // GIVEN
        String key = subscriptionId + "_2026-08";
        when(idempotencyService.generateKey(any(), any()))
                .thenReturn(key);
        when(idempotencyService.invoiceAlreadyExists(key))
                .thenReturn(true);
        when(invoiceRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingInvoice));

        // WHEN
        Invoice result = invoiceService.createRecurringInvoice(
                subscriptionId, userId, "ahmed@email.com", planId,
                new BigDecimal("30.00"), LocalDate.of(2026, 8, 1)
        );

        // THEN
        assertThat(result.getId())
                .isEqualTo(existingInvoice.getId());

        // PAS de nouveau save
        verify(invoiceRepository, never()).save(any());

        // PAS de nouvel event
        verify(eventPublisher, never())
                .publishInvoiceGenerated(any(Invoice.class), any(UUID.class));
    }

    @Test
    @DisplayName("markAsPaid → status PAID + paidAt rempli")
    void markAsPaid_shouldUpdateStatusAndPaidAt() {
        // GIVEN
        UUID invoiceId = existingInvoice.getId();
        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(any()))
                .thenReturn(existingInvoice);

        // WHEN
        invoiceService.markAsPaid(invoiceId);

        // THEN
        verify(invoiceRepository).save(
                argThat(invoice ->
                        invoice.getStatus() == InvoiceStatus.PAID
                                && invoice.getPaidAt() != null
                )
        );
    }

    @Test
    @DisplayName("markAsFailed → status FAILED")
    void markAsFailed_shouldUpdateStatus() {
        // GIVEN
        UUID invoiceId = existingInvoice.getId();
        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(any()))
                .thenReturn(existingInvoice);

        // WHEN
        invoiceService.markAsFailed(invoiceId);

        // THEN
        verify(invoiceRepository).save(
                argThat(invoice ->
                        invoice.getStatus() == InvoiceStatus.FAILED
                )
        );
    }
}