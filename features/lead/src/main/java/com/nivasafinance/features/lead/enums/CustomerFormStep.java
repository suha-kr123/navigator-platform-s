package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum CustomerFormStep {

    BASIC_INFO("basicInfo"),
    LOCATION_DETAILS("locationDetails"),
    PROPERTY_DETAILS("propertyDetails"),
    INCOME_DETAILS("incomeDetails"),
    PROPERTY_DOCUMENTS("propertyDocuments"),
    CB_CONSENT("cbConsent"),
    TERMINAL("terminal"),
    SCHEDULE_VISIT("scheduleVisit")
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
