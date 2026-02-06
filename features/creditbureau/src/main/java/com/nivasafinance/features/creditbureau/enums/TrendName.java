package com.nivasafinance.features.creditbureau.enums;

/**
 * Known bureau score trend types from credit bureau reports.
 * Maps from CRIF/bureau report TRENDS.NAME values to a stable enum for API responses.
 */
public enum TrendName {
    CREDIT_SCORE,
    UNKNOWN;

    /**
     * Parses trend name from the raw string value stored in the report/entity.
     * Returns UNKNOWN for null, empty or unrecognized values.
     */
    public static TrendName fromString(String name) {
        if (name == null || name.isBlank()) {
            return UNKNOWN;
        }
        String normalized = name.trim().toLowerCase();
        return switch (normalized) {
            case "credit score", "credit_score" -> TrendName.CREDIT_SCORE;
            default -> TrendName.UNKNOWN;
        };
    }
}
