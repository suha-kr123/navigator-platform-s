package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum LeadStatus {

    ACTIVE(1L, "ENQUIRY", "Enquiry"),
    REJECTED(2L, "REJECTED", "Rejected"),
    WITHDRAWN(3L, "WITHDRAWN", "Withdrawn"),
    COMPLETED(7L, "COMPLETED", "Completed"),

    ;

    private final Long id;
    private final String code;
    private final String value;

    LeadStatus(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

}
