package com.saas.billing.billing_service.service;

import com.saas.billing.billing_service.domain.entity.Invoice;
import com.saas.billing.billing_service.messaging.event.SubscriptionCreatedEvent;
import com.saas.billing.billing_service.messaging.event.PlanChangedEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BillingService {

    private final InvoiceService invoiceService;

    public BillingService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public void billFirstSubscription(
            SubscriptionCreatedEvent event
    ) {

        invoiceService.createFirstInvoiceAfterPayment(
                event.subscriptionId(),
                event.paymentId(),
                event.userId(),
                event.userEmail(),
                event.planPrice(),
                event.startDate()
        );
    }
    // ════════════════════════════════════
    // FACTURER UN ABONNEMENT
    // appelé par le scheduler chaque nuit
    // ════════════════════════════════════

    public void billSubscription(
            java.util.UUID subscriptionId,
            java.util.UUID userId,
            String userEmail,
            java.math.BigDecimal amount,
            LocalDate billingDate) {

        invoiceService.createRecurringInvoice(
                subscriptionId,
                userId,
                userEmail,
                amount,
                billingDate
        );
    }

    // ════════════════════════════════════
    // FACTURER UN UPGRADE (PRORATION)
    // appelé quand subscription-service
    // publie PlanChanged avec type UPGRADE
    // ════════════════════════════════════

    public void billUpgrade(PlanChangedEvent event) {
        if (!"UPGRADE".equals(event.changeType())) return;

        LocalDate upgradeDate = event.effectiveDate();
        LocalDate periodEnd = upgradeDate
                .withDayOfMonth(upgradeDate.lengthOfMonth());

        invoiceService.createProrataInvoice(
                event.subscriptionId(),
                event.userId(),
                event.userEmail(),
                event.prorataAmount(),
                upgradeDate,
                periodEnd
        );
    }
}