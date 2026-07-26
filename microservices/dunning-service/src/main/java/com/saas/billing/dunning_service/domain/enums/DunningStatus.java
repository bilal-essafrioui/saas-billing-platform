package com.saas.billing.dunning_service.domain.enums;

public enum DunningStatus {
    SCHEDULED,   // planifié, pas encore exécuté
    PROCESSING,  // en cours d'exécution
    SUCCEEDED,   // paiement réussi
    FAILED,      // paiement échoué
    CANCELLED    // annulé (paiement réussi entre-temps)
}