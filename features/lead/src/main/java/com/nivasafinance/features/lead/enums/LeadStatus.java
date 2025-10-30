package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum LeadStatus {

    ENQUIRY(1L, "ENQUIRY", "Enquiry"),
    REJECTED(2L, "REJECTED", "Rejected"),
    WITHDRAWN(3L, "WITHDRAWN", "Withdrawn"),
    QUALIFIED(4L, "QUALIFIED", "Qualified"),
    SUBMITTED(5L, "SUBMITTED", "Submitted"),
    DISBURSED(6L, "DISBURSED", "Disbursed"),
    COMPLETED(7L, "COMPLETED", "Completed"),
    DROP_OFF(8L, "DROP_OFF", "Drop Off");

    private final Long id;
    private final String code;
    private final String value;

    LeadStatus(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

}
