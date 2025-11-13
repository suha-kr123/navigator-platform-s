package com.nivasafinance.features.advisor.enums;

import lombok.Getter;

@Getter
public enum BankDetailsStatus {

    ACTIVE(1L, "ACTIVE", "Active"),
    DEACTIVATED(2L, "DEACTIVATED", "Deactivated");

    private final Long id;
    private final String code;
    private final String value;

    BankDetailsStatus(Long id, String code, String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}

