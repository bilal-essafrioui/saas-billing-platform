package com.saas.billing.billing_service.domain.enums;

public enum PeriodValue {
    ALL_TIME(null),
    LAST_3_MONTHS(3),
    LAST_6_MONTHS(6),
    LAST_12_MONTHS(12);

    private final Integer months;

    PeriodValue(Integer months) {
        this.months = months;
    }

    public Integer getMonths() {
        return months;
    }
}
