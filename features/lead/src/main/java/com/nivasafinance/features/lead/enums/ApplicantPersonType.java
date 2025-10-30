package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum ApplicantPersonType {

    DECISION_MAKER(1L, "DECISION_MAKER", "Decision Maker"),
    PRIMARY(2L, "PRIMARY", "Primary"),
    PROPERTY_OWNER(3L, "PROPERTY_OWNER", "Property Owner");

    private final Long id;
    private final String code;
    private final String value;

    ApplicantPersonType(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }
}
