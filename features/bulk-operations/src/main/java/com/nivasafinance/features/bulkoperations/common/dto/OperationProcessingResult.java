package com.nivasafinance.features.bulkoperations.common.dto;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;

import java.util.List;

public record OperationProcessingResult(
        BulkOperationStatus finalStatus,
        int totalRows,
        int successCount,
        int failureCount,
        String summaryStorageKey,
        String errorMessage,
        List<CsvReportRow> successRows,
        List<CsvReportRow> failedRows) {

    public static OperationProcessingResult failed(BulkOperation bulkOperation, String errorMessage) {
        return new OperationProcessingResult(
                BulkOperationStatus.FAILED,
                0, 0, 0,
                null,
                errorMessage,
                List.of(),
                List.of());
    }
}
