package com.nivasafinance.externals.exotel.service;

import com.nivasafinance.externals.exotel.dto.ExotelReconciliationSummary;

import java.time.LocalDate;

public interface ExotelReconciliationService {

    ExotelReconciliationSummary reconcilePreviousDay();

    /**
     * Reconciles all Exotel {@code n_call_log} rows (no created-at window). Intended for a one-off
     * historical backfill; enable via {@code exotel.reconciliation.historical-backfill.enabled=true}.
     */
    ExotelReconciliationSummary reconcileAllHistorical();

    /**
     * Reconciles rows with {@code created_at} in [{@code startDate}, {@code endDate}] (inclusive calendar days,
     * using {@code exotel.reconciliation.zone} or system default for day boundaries).
     */
    ExotelReconciliationSummary reconcileDateRange(LocalDate startDate, LocalDate endDate);
}
