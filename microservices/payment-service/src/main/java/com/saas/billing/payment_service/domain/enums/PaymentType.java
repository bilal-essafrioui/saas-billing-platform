package com.saas.billing.payment_service.domain.enums;

public enum PaymentType {
    INITIAL_SUBSCRIPTION,  // premier paiement
    RENEWAL,               // renouvellement mensuel
    UPGRADE_PRORATION,     // upgrade en cours de mois
    DOWNGRADE,
    RETRY                  // relance dunning
}