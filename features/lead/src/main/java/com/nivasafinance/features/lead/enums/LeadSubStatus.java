package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum LeadSubStatus {

    ONHOLD(1L, "ONHOLD", "ONHOLD"),
    ;

    private final Long id;
    private final String code;
    private final String value;

    LeadSubStatus(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

}
