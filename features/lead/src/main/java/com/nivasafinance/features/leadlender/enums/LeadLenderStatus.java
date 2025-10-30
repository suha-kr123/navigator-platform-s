package com.nivasafinance.features.leadlender.enums;

public enum LeadLenderStatus {
    SELECTED,
    SUBMITTED,
    REJECTED;

    public boolean isInProgress() {
        return this == SELECTED || this == SUBMITTED;
    }

    public boolean isCompletedOrRejected() {
        return this == REJECTED;
    }
}

