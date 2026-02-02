package com.nivasafinance.features.bulkoperations.common.utils;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationCsvValidationException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationReportLayout;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class CsvReportGenerator {

    private static final String REPORT_GENERATION_FAILED_MESSAGE = "Failed to generate CSV report";
    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final String ERROR_GENERATING_UNIFIED_REPORT = "Error generating unified report: {}";

    private CsvReportGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a unified CSV report using the layout for the operation type.
     * Report structure is driven by {@link BulkOperationReportLayout}; no generator changes needed for new types.
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

        List<UnifiedReportEntry> entries = new ArrayList<>();
        if (!ValidationUtils.isNullOrEmpty(validationErrors)) {
            for (CsvValidationError err : validationErrors) {
                CsvReportRow row = layout.toCsvReportRowForValidationError(err);
                entries.add(new UnifiedReportEntry(row, VALIDATION_FAILED));
            }
        }
        if (!ValidationUtils.isNullOrEmpty(successRows)) {
            for (CsvReportRow row : successRows) {
                entries.add(new UnifiedReportEntry(row, SUCCESS));
            }
        }
        if (!ValidationUtils.isNullOrEmpty(failedRows)) {
            for (CsvReportRow row : failedRows) {
                entries.add(new UnifiedReportEntry(row, FAILED));
            }
        }
        entries.sort((a, b) -> Integer.compare(a.row().getRowNumber(), b.row().getRowNumber()));

        List<String> headers = layout.getReportHeaders();
        if (ValidationUtils.isNullOrEmpty(headers)) {
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE);
        }

        try (StringWriter writer = new StringWriter();
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord(headers);
            for (UnifiedReportEntry entry : entries) {
                csvPrinter.printRecord(layout.buildReportRow(entry.row(), entry.status()));
            }
            csvPrinter.flush();
            return writer.toString();
        } catch (IOException e) {
            log.error(ERROR_GENERATING_UNIFIED_REPORT, bulkOperation.getId(), e);
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE, e);
        }
    }

    private record UnifiedReportEntry(CsvReportRow row, String status) {}
}
