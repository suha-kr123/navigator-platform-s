package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;

import java.util.Collections;
import java.util.Map;

/**
 * Base for type-specific report layouts. Subclasses implement {@link #getType()}, {@link #getReportHeaders()},
 * and {@link #buildReportRow(CsvReportRow, String)}.
 */
public abstract class AbstractBulkOperationReportLayout implements BulkOperationReportLayout {

    protected static final String EMPTY_STRING = "";

    protected static String getEntityIdString(CsvReportRow row) {
        return ValidationUtils.isNonNull(row.getRowReference()) ? row.getRowReference() : EMPTY_STRING;
    }

    protected static String getErrorMessage(CsvReportRow row) {
        return ValidationUtils.isNonNullOrEmpty(row.getErrorMessage()) ? row.getErrorMessage() : EMPTY_STRING;
    }

    protected static String getValue(Map<?, ?> map, String key) {
        if (ValidationUtils.isEmpty(map) || !map.containsKey(key)) {
            return EMPTY_STRING;
        }
        Object val = map.get(key);
        return ValidationUtils.isNonNull(val) ? val.toString() : EMPTY_STRING;
    }

    protected static Map<String, Object> originalValues(CsvReportRow row) {
        return row.getOriginalValues() != null ? row.getOriginalValues() : Collections.emptyMap();
    }

    protected static Map<String, Object> newValues(CsvReportRow row) {
        return row.getNewValues() != null ? row.getNewValues() : Collections.emptyMap();
    }

    protected static Map<String, String> rowData(CsvReportRow row) {
        return row.getRowData() != null ? row.getRowData() : Collections.emptyMap();
    }
}
