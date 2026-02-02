package com.nivasafinance.features.bulkoperations.engine;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.nivasafinance.features.bulkoperations.common.exception.InvalidBulkOperationTypeException;

@Getter
public enum BulkOperationType {

    DROPOFF(
            "DROPOFF",
            "Dropoff Lead",
            "Bulk dropoff leads with reason codes",
            Arrays.asList("lead_identifier", "reason_code"),
            Collections.emptyList()),

    ONHOLD(
            "ONHOLD",
            "On Hold Lead",
            "Bulk put leads on hold with reason and follow-up date",
            Arrays.asList("lead_identifier", "reason_code", "follow_up_date"),
            Collections.emptyList()),

    REJECTED(
            "REJECTED",
            "Reject Lead",
            "Bulk reject leads with reason codes",
            Arrays.asList("lead_identifier", "reason_code"),
            Collections.emptyList());

    private final String identifier;
    private final String displayName;
    private final String description;
    private final List<String> requiredColumns;
    private final List<String> optionalColumns;

    BulkOperationType(
            String identifier,
            String displayName,
            String description,
            List<String> requiredColumns,
            List<String> optionalColumns) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.description = description;
        this.requiredColumns = requiredColumns;
        this.optionalColumns = optionalColumns;
    }

    public static BulkOperationType fromIdentifier(String identifier) {
        return Arrays.stream(values())
                .filter(type -> type.identifier.equalsIgnoreCase(identifier))
                .findFirst()
                .orElseThrow(() -> new InvalidBulkOperationTypeException(identifier));
    }
}
