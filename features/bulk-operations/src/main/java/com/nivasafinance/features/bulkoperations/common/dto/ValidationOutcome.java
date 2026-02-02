package com.nivasafinance.features.bulkoperations.common.dto;

import java.util.List;
import java.util.Map;

/**
 * Result of business rule validation. Contains valid rows and per-row validation errors.
 */
public record ValidationOutcome(
        List<Map<String, Object>> validRows,
        List<CsvValidationError> errors) {

    public static ValidationOutcome of(List<Map<String, Object>> validRows, List<CsvValidationError> errors) {
        return new ValidationOutcome(
                validRows != null ? validRows : List.of(),
                errors != null ? errors : List.of());
    }
}
