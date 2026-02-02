package com.nivasafinance.features.bulkoperations.common.utils;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationCsvValidationException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationReportLayout;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class CsvReportGenerator {

    private static final String REPORT_GENERATION_FAILED_MESSAGE = "Failed to generate CSV report";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final String ERROR_GENERATING_UNIFIED_REPORT = "Error generating unified report: {}";

    private CsvReportGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a unified CSV report by streaming rows one-by-one (validation errors, then success, then failed).
     * No combined list or sort; rows are written in order so we avoid holding all entries in memory.
     */
    public static String generateUnifiedReport(
            BulkOperation bulkOperation,
            List<CsvValidationError> validationErrors,
            List<CsvReportRow> successRows,
            List<CsvReportRow> failedRows,
            BulkOperationReportLayout layout) {
        ValidationUtils.requireNonNull(bulkOperation,
                () -> new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE));
        ValidationUtils.requireNonNull(layout,
                () -> new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE));

        CsvReportStreamWriter streamWriter = new CsvReportStreamWriter(layout);
        try {
            streamWriter.writeHeader();

            if (!ValidationUtils.isNullOrEmpty(validationErrors)) {
                for (CsvValidationError err : validationErrors) {
                    streamWriter.writeValidationError(err);
                }
            }
            if (!ValidationUtils.isNullOrEmpty(successRows)) {
                for (CsvReportRow row : successRows) {
                    streamWriter.writeReportRow(row, SUCCESS);
                }
            }
            if (!ValidationUtils.isNullOrEmpty(failedRows)) {
                for (CsvReportRow row : failedRows) {
                    streamWriter.writeReportRow(row, FAILED);
                }
            }
            if (streamWriter.getRowCount() == 0) {
                streamWriter.writeValidationError(CsvValidationError.builder()
                        .rowNumber(1)
                        .errorCode("NO_ROWS")
                        .errorMessage("No rows to report")
                        .rowReference("")
                        .build());
            }

            return streamWriter.getContent();
        } catch (Exception e) {
            log.error(ERROR_GENERATING_UNIFIED_REPORT, bulkOperation.getId(), e);
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE, e);
        } finally {
            streamWriter.close();
        }
    }
}
