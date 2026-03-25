package com.nivasafinance.externals.exotel.dto;

/**
 * Aggregates from one reconciliation run (scheduled, manual range, or historical backfill).
 */
public record ExotelReconciliationSummary(
        int rowsFetched,
        int rowsUpdated,
        int statusCorrections,
        int durationCorrections,
        int apiFailures,
        int skippedInvalidRows) {
}
