package com.saas.billing.payment_service.service;

import com.saas.billing.payment_service.domain.entity.PaymentMethod;
import com.saas.billing.payment_service.dto.response.PaymentMethodResponse;
import com.saas.billing.payment_service.exception.PaymentMethodNotFoundException;
import com.saas.billing.payment_service.repository.PaymentMethodRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod.Card;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodService(
            PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    // ════════════════════════════════════
    // SAUVEGARDER LE MOYEN DE PAIEMENT
    // appelé après un paiement réussi
    // extrait les infos depuis Stripe
    // ════════════════════════════════════

    @Transactional
    public void savePaymentMethod(
            UUID userId,
            String stripeCustomerId,
            String stripePaymentMethodId,
            String brand,
            String last4,
            Integer expMonth,
            Integer expYear) {

        // désactiver l'ancien si existe
        paymentMethodRepository
                .findByUserIdAndActiveTrue(userId)
                .ifPresent(old -> {
                    old.setActive(false);
                    paymentMethodRepository.save(old);
                });

        // sauvegarder le nouveau
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .userId(userId)
                .stripeCustomerId(stripeCustomerId)
                .stripePaymentMethodId(stripePaymentMethodId)
                .brand(brand)
                .last4(last4)
                .expMonth(expMonth)
                .expYear(expYear)
                .isDefault(true)
                .active(true)
                .build();

        paymentMethodRepository.save(paymentMethod);
    }

    // ════════════════════════════════════
    // GET MON MOYEN DE PAIEMENT
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public PaymentMethodResponse getMyPaymentMethod(UUID userId) {
        PaymentMethod pm = paymentMethodRepository
                .findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new PaymentMethodNotFoundException(
                        "No payment method found for user : " + userId
                ));
        return toResponse(pm);
    }

    // ════════════════════════════════════
    // GET PAYMENT METHOD ENTITY
    // utilisé en interne
    // ════════════════════════════════════

    @Transactional(readOnly = true)
    public PaymentMethod getPaymentMethodEntity(UUID userId) {
        return paymentMethodRepository
                .findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new PaymentMethodNotFoundException(
                        "No payment method found for user : " + userId
                ));
    }

    @Transactional(readOnly = true)
    public boolean hasPaymentMethod(UUID userId) {
        return paymentMethodRepository
                .existsByUserIdAndActiveTrue(userId);
    }

    private PaymentMethodResponse toResponse(PaymentMethod pm) {
        return PaymentMethodResponse.builder()
                .id(pm.getId())
                .userId(pm.getUserId())
                .brand(pm.getBrand())
                .last4(pm.getLast4())
                .expMonth(pm.getExpMonth())
                .expYear(pm.getExpYear())
                .isDefault(pm.isDefault())
                .active(pm.isActive())
                .build();
    }

    // ════════════════════════════════════
    // REMPLACER LE MOYEN DE PAIEMENT
    // appelé après un SetupIntent réussi
    // ════════════════════════════════════

    @Transactional
    public void replacePaymentMethod(
            UUID userId,
            String stripeCustomerId,
            String stripePaymentMethodId,
            String brand,
            String last4,
            Integer expMonth,
            Integer expYear) {

        paymentMethodRepository
                .findByUserIdAndActiveTrue(userId)
                .ifPresent(old -> {
                    old.setActive(false);
                    old.setDefault(false);
                    paymentMethodRepository.save(old);
                });

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .userId(userId)
                .stripeCustomerId(stripeCustomerId)
                .stripePaymentMethodId(stripePaymentMethodId)
                .brand(brand)
                .last4(last4)
                .expMonth(expMonth)
                .expYear(expYear)
                .isDefault(true)
                .active(true)
                .build();

        paymentMethodRepository.save(paymentMethod);
    }

    @Transactional(readOnly = true)
    public PaymentMethod getByStripeCustomerId(
            String stripeCustomerId
    ) {
        return paymentMethodRepository
                .findByStripeCustomerIdAndActiveTrue(stripeCustomerId)
                .orElseThrow(() ->
                        new PaymentMethodNotFoundException(
                                "No payment method found for Stripe customer : "
                                        + stripeCustomerId
                        )
                );
    }
}