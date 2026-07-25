package com.saas.billing.notification_service.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.billing.notification_service.messaging.KafkaTopics;
import com.saas.billing.notification_service.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(
            EmailService emailService,
            ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════
    //  OTP_GENERATED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.OTP_GENERATED,
            groupId = "notification-service"
    )
    public void handleOtpGenerated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("email").asText();
            String otp = node.get("otp").asText();
            String expirationMinutes = node.get("expirationMinutes").asText();

            emailService.sendEmail(
                    email,
                    "Your BillFlow OTP Verification Code",
                    buildOtpGeneratedBody(otp, expirationMinutes)
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing subscription-created : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // SUBSCRIPTION CREATED OTP_GENERATED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.SUBSCRIPTION_CREATED,
            groupId = "notification-service"
    )
    public void handleSubscriptionCreated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();
            String planName = node.get("planName").asText();
            String nextRenewal = node.get("nextRenewalDate").asText();

            emailService.sendEmail(
                    email,
                    "Welcome to BillFlow!",
                    buildSubscriptionCreatedBody(planName, nextRenewal)
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing subscription-created : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // SUBSCRIPTION CANCELLED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.SUBSCRIPTION_CANCELLED,
            groupId = "notification-service"
    )
    public void handleSubscriptionCancelled(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();
            String planName = node.get("planName").asText();

            emailService.sendEmail(
                    email,
                    "Your subscription has been canceled.",
                    buildSubscriptionCancelledBody(planName)
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing subscription-cancelled : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // DOWNGRADE SCHEDULED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.DOWNGRADE_SCHEDULED,
            groupId = "notification-service"
    )
    public void handleDowngradeScheduled(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();
            String currentPlan = node.get("currentPlanName").asText();
            String newPlan = node.get("newPlanName").asText();
            String effectiveDate = node.get("effectiveDate").asText();

            emailService.sendEmail(
                    email,
                    "Scheduled plan change",
                    buildDowngradeScheduledBody(
                            currentPlan, newPlan, effectiveDate
                    )
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing downgrade-scheduled : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // PlanChanged
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PLAN_CHANGED,
            groupId = "notification-service"
    )
    public void handlePlanChanged(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();
            String previousPlanName = node.get("previousPlanName").asText();
            String previousPlanPrice = node.get("previousPlanPrice").asText();
            String newPlanName = node.get("newPlanName").asText();
            String newPlanPrice = node.get("newPlanPrice").asText();
            String changeType = node.get("changeType").asText();
            String prorataAmount = node.get("prorataAmount").asText();
            String effectiveDate = node.get("effectiveDate").asText();
            String subject = changeType.equals("UPGRADE")
                    ? "Your subscription has been upgraded"
                    : "Your subscription change has been downgraded";

            emailService.sendEmail(
                    email,
                    subject,
                    buildPlanChangedBody(
                            previousPlanName, previousPlanPrice, newPlanName, newPlanPrice, prorataAmount, effectiveDate
                    )
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing downgrade-scheduled : "
                            + e.getMessage()
            );
        }
    }


    // ════════════════════════════════════
    // DOWNGRADE APPLIED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.DOWNGRADE_APPLIED,
            groupId = "notification-service"
    )
    public void handleDowngradeApplied(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();
            String previousPlan = node.get("previousPlanName").asText();
            String newPlan = node.get("newPlanName").asText();
            String newPrice = node.get("newPlanPrice").asText();

            emailService.sendEmail(
                    email,
                    "Your plan has been updated.",
                    buildDowngradeAppliedBody(
                            previousPlan, newPlan, newPrice
                    )
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing downgrade-applied : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // INVOICE GENERATED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.INVOICE_GENERATED,
            groupId = "notification-service"
    )
    public void handleInvoiceGenerated(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();
            String amount = node.get("amount").asText();
            String periodStart = node.get("billingPeriodStart").asText();
            String periodEnd = node.get("billingPeriodEnd").asText();

            emailService.sendEmail(
                    email,
                    "Your BillFlow invoice is now available.",
                    buildInvoiceGeneratedBody(
                            amount, periodStart, periodEnd
                    )
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing invoice-generated : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // PAYMENT SUCCEEDED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_SUCCEEDED,
            groupId = "notification-service"
    )
    public void handlePaymentSucceeded(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            // userEmail peut être null dans cet event
            // selon ce que payment-service publie
            if (!node.has("userEmail")
                    || node.get("userEmail").isNull()) {
                return;
            }

            String email = node.get("userEmail").asText();
            String amount = node.get("amount").asText();
            String currency = node.get("currency").asText();

            emailService.sendEmail(
                    email,
                    "Payment confirmed",
                    buildPaymentSucceededBody(amount, currency)
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing payment-succeeded : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // PAYMENT FAILED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "notification-service"
    )
    public void handlePaymentFailed(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);

            if (!node.has("userEmail")
                    || node.get("userEmail").isNull()) {
                return;
            }

            String email = node.get("userEmail").asText();
            String amount = node.get("amount").asText();
            int attemptNumber = node.has("attemptNumber")
                    ? node.get("attemptNumber").asInt()
                    : 1;

            emailService.sendEmail(
                    email,
                    "Payment failed",
                    buildPaymentFailedBody(amount, attemptNumber)
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing payment-failed : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // SUBSCRIPTION SUSPENDED
    // ════════════════════════════════════

    @KafkaListener(
            topics = KafkaTopics.SUBSCRIPTION_SUSPENDED,
            groupId = "notification-service"
    )
    public void handleSubscriptionSuspended(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email = node.get("userEmail").asText();

            emailService.sendEmail(
                    email,
                    "Your subscription has been suspended.",
                    buildSubscriptionSuspendedBody()
            );
        } catch (Exception e) {
            System.err.println(
                    "Error processing subscription-suspended : "
                            + e.getMessage()
            );
        }
    }

    // ════════════════════════════════════
    // EMAIL TEMPLATES
    // ════════════════════════════════════
    private String buildOtpGeneratedBody(
            String otp,
            String expirationMinutes) {
        return """
            Hello,
            
            Welcome to BillFlow!
            
            To complete your action, please use the following One-Time Password (OTP):
            
            OTP: %s
            This code will expire in %s minutes.
            
            If you did not request this code, please ignore this email or contact our support team immediately.
            
            For security reasons, never share this OTP with anyone.
            
            Thank you for choosing BillFlow.
            
            Best regards,
            The BillFlow Team
            """.formatted(otp, expirationMinutes);
    }

    private String buildSubscriptionCreatedBody(
            String planNom,
            String nextRenewal) {
        return """
            Hello,
            
            Welcome to BillFlow!
            
            Your subscription is now active.
            
            Plan: %s
            Next renewal: %s
            
            Thank you for choosing BillFlow.
            
            The BillFlow Team
            """.formatted(planNom, nextRenewal);
    }

    private String buildPlanChangedBody(
            String previousPlanName,
            String previousPlanPrice,
            String newPlanName,
            String newPlanPrice,
            String prorataAmount,
            String effectiveDate) {
        return """
            Hello,
            
            Your subscription has been successfully upgraded.
            
            Subscription details
            ────────────────────────
            Previous plan: %s ($%s/month)
            New plan: %s ($%s/month)
            
            Effective date: J%s
            Prorated charge: $%s
            
            Your new plan is now active, and you can immediately access all Enterprise features. The prorated amount has been charged to your saved payment method. Your future billing cycles will renew at $%s per month unless your subscription changes.
            
            If you didn't authorize this change or believe there has been a mistake, please contact our support team as soon as possible.
            
            Thank you for choosing BillFlow.
            
            Best regards,
            The BillFlow Team
            """.formatted(previousPlanName, previousPlanPrice, newPlanName, newPlanPrice, effectiveDate, prorataAmount, newPlanPrice);
    }

    private String buildSubscriptionCancelledBody(String planNom) {
        return """
            Hello,
            
            Your %s subscription has been successfully canceled.
            
            You will continue to have access to your current plan
            until the end of your current billing period.
            
            We hope to see you again soon.
            
            The BillFlow Team
            """.formatted(planNom);
    }

    private String buildDowngradeScheduledBody(
            String currentPlan,
            String newPlan,
            String effectiveDate) {
        return """
            Hello,
            
            Your plan change request has been successfully scheduled.
            
            Current plan: %s
            New plan: %s
            Effective date: %s
            
            No payment will be charged at this time.
            The change will be applied automatically
            on the scheduled date.
            
            The BillFlow Team
            """.formatted(currentPlan, newPlan, effectiveDate);
    }

    private String buildDowngradeAppliedBody(
            String previousPlan,
            String newPlan,
            String newPrice) {
        return """
            Hello,
            
            Your plan has been successfully updated.
            
            Previous plan: %s
            New plan: %s
            New monthly price: $%s
            
            The BillFlow Team
            """.formatted(previousPlan, newPlan, newPrice);
    }

    private String buildInvoiceGeneratedBody(
            String montant,
            String periodStart,
            String periodEnd) {
        return """
            Hello,
            
            Your BillFlow invoice is now available.
            
            Amount: $%s
            Billing period: %s to %s
            
            You can view it from your dashboard.
            
            The BillFlow Team
            """.formatted(montant, periodStart, periodEnd);
    }

    private String buildPaymentSucceededBody(
            String amount,
            String currency) {
        return """
            Hello,
            
            Your payment has been successfully confirmed.
            
            Amount charged: %s %s
            
            Thank you for choosing BillFlow.
            
            The BillFlow Team
            """.formatted(amount, currency.toUpperCase());
    }

    private String buildPaymentFailedBody(
            String amount,
            int attemptNumber) {

        String nextRetry = switch (attemptNumber) {
            case 1 -> "A new payment attempt will be made in 24 hours (Day 1).";
            case 2 -> "A new payment attempt will be made in 3 days (Day 3).";
            case 3 -> "A new payment attempt will be made in 7 days (Day 7).";
            default -> "Your account will be suspended if the payment continues to fail.";
        };

        return """
            Hello,
            
            Your payment of $%s has failed.
            
            %s
            
            To avoid your account being suspended,
            please update your payment method
            from your dashboard.
            
            The BillFlow Team
            """.formatted(amount, nextRetry);
    }

    private String buildSubscriptionSuspendedBody() {
        return """
            Hello,
            
            Your subscription has been suspended due to
            multiple consecutive payment failures.
            
            To reactivate your account, please
            update your payment method
            from your dashboard.
            
            The BillFlow Team
            """;
    }
}
