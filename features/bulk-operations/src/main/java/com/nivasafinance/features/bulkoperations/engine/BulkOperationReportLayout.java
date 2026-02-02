package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines report structure (headers and row format) per bulk operation type.
 * New types implement this interface or extend a base; no changes to CsvReportGenerator needed.
 */
public interface BulkOperationReportLayout {

    BulkOperationType getType();

    List<String> getReportHeaders();

    List<String> buildReportRow(CsvReportRow row, String rowStatus);

    /**
     * Column that receives the row reference for validation error rows (e.g. lead_identifier, order_id).
     * Keeps the engine generic; layouts override for domain-specific naming.
     */
    default String getRowReferenceColumnName() {
        return "row_reference";
    }

    /**
     * Builds CsvReportRow for a validation error. Uses {@link #getRowReferenceColumnName()} for rowData when
     * rowDataMap is null; otherwise uses error.getRowDataMap() so each invalid row shows full CSV column data.
     */
    default CsvReportRow toCsvReportRowForValidationError(CsvValidationError error) {
        String rowRef = error.getRowReference();
        Map<String, String> rowData = error.getRowDataMap() != null && !error.getRowDataMap().isEmpty()
                ? new LinkedHashMap<>(error.getRowDataMap())
                : Collections.singletonMap(getRowReferenceColumnName(), rowRef != null ? rowRef : "");
        return CsvReportRow.builder()
                .rowNumber(error.getRowNumber() != null ? error.getRowNumber() : 0)
                .rowReference(rowRef)
                .errorMessage(error.getErrorMessage())
                .originalValues(Collections.emptyMap())
                .newValues(Collections.emptyMap())
                .rowData(rowData)
                .build();
    }
}
