package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum CustomerFormStep {

    PROPERTY_DETAILS("propertyDetails"),
    INCOME_DETAILS("incomeDetails"),
    PROPERTY_DOCUMENTS("propertyDocuments"),
    CB_CONSENT("cbConsent"),
    ;
    private final String key;

    CustomerFormStep(String key) {
        this.key = key;
    }

    public static CustomerFormStep fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        for (CustomerFormStep step : values()) {
            if (step.key.equals(key)) {
                return step;
            }
        }
        return null;
    }
}
