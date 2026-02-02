package com.nivasafinance.features.bulkoperations.common.utils;

/**
 * Standard keys for validated row maps.
 * Validators should put {@link #ROW_NUMBER} in each row; processors may use additional domain-specific keys.
 * <p>
 * Row numbering: row 1 is the CSV header; first data row is row 2. Validators should assign
 * row numbers accordingly (e.g. start at 2 for the first parsed record).
 */
public final class RowDataKeys {

    /** Row number. Row 1 = header; first data row = 2. Validators must set this. */
    public static final String ROW_NUMBER = "rowNumber";

    private RowDataKeys() {
    }
}
