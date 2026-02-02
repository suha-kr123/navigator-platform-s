package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.OperationProcessingResult;
import com.nivasafinance.features.bulkoperations.common.dto.ProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.enums.RowProcessingStatus;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.bulkoperations.common.utils.WorkingFileCsvUtils;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationRowTransactionRunner;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base processor that orchestrates: load validated rows from working file → process rows in batches → build unified report.
 * Progress (currentBatch, totalBatches, processedRows) is persisted after each batch to support visibility and timeout.
 * Subclasses implement {@link #getType()}, {@link #processRow}, {@link #toRowDataMap}, and {@link #getRowReference}.
 */
@Slf4j
public abstract class BaseBulkOperationProcessor implements BulkOperationProcessor {

    private static final String FAILED_TO_LOAD_WORKING_FILE = "Failed to load working file for bulk operation {}";

    protected final BulkOperationReportService reportService;
    protected final BulkOperationFileStorageService fileStorageService;
    protected final BulkOperationRepository bulkOperationRepository;
    protected final BulkOperationProcessingPersistence persistence;
    protected final BulkOperationRowTransactionRunner rowTransactionRunner;
    protected final BulkOperationCsvProperties csvProperties;
    protected final BulkOperationExceptionFactory exceptionFactory;
    protected final int batchSize;

    protected BaseBulkOperationProcessor(
            BulkOperationReportService reportService,
            BulkOperationFileStorageService fileStorageService,
            BulkOperationRepository bulkOperationRepository,
            BulkOperationProcessingPersistence persistence,
            BulkOperationRowTransactionRunner rowTransactionRunner,
            BulkOperationCsvProperties csvProperties,
            BulkOperationExceptionFactory exceptionFactory,
            int batchSize) {
        this.reportService = reportService;
        this.fileStorageService = fileStorageService;
        this.bulkOperationRepository = bulkOperationRepository;
        this.persistence = persistence;
        this.rowTransactionRunner = rowTransactionRunner;
        this.csvProperties = csvProperties;
        this.exceptionFactory = exceptionFactory;
        this.batchSize = Math.max(1, batchSize);
    }

    @Override
    public OperationProcessingResult process(BulkOperation bulkOperation) {
        List<Map<String, Object>> validRows = loadValidRowsFromWorkingFile(bulkOperation);

        if (ValidationUtils.isNullOrEmpty(validRows)) {
            return new OperationProcessingResult(
                    BulkOperationStatus.COMPLETED, 0, 0, 0, null, null, List.of(), List.of());
        }

        int maxRows = csvProperties.getMaxRows();
        if (validRows.size() > maxRows) {
            throw exceptionFactory.bulkOperationCsvValidationRowCountExceededException(maxRows);
        }

        int totalRows = validRows.size();
        int totalBatches = (int) Math.ceil((double) totalRows / batchSize);
        List<CsvReportRow> successRows = new ArrayList<>();
        List<CsvReportRow> failedRows = new ArrayList<>();

        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            int from = batchIndex * batchSize;
            int to = Math.min(from + batchSize, totalRows);
            List<Map<String, Object>> batch = validRows.subList(from, to);

            for (Map<String, Object> row : batch) {
                ProcessingResult result;
                try {
                    result = rowTransactionRunner.runInNewTransaction(() -> processRow(bulkOperation, row));
                } catch (Exception e) {
                    result = ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), toOriginalMap(row));
                }
                CsvReportRow reportRow = toCsvReportRow(row, result);
                if (result.status() == RowProcessingStatus.SUCCESS) {
                    successRows.add(reportRow);
                } else {
                    failedRows.add(reportRow);
                }
            }

            int processedCount = successRows.size() + failedRows.size();
            persistence.persistBatchProgress(bulkOperation.getOperationIdentifier(), batchIndex + 1, totalBatches, processedCount);
        }

        int total = successRows.size() + failedRows.size();
        BulkOperationStatus finalStatus = failedRows.isEmpty()
                ? (bulkOperation.getIsDryRun() ? BulkOperationStatus.DRY_RUN_COMPLETED : BulkOperationStatus.COMPLETED)
                : (successRows.isEmpty() ? BulkOperationStatus.FAILED : BulkOperationStatus.PARTIALLY_COMPLETED);

        return new OperationProcessingResult(
                finalStatus,
                total,
                successRows.size(),
                failedRows.size(),
                null,
                null,
                successRows,
                failedRows);
    }

    private Map<String, Object> toOriginalMap(Map<String, Object> row) {
        return row != null ? Map.copyOf(row) : Map.of();
    }

    private CsvReportRow toCsvReportRow(Map<String, Object> row, ProcessingResult result) {
        return CsvReportRow.builder()
                .rowNumber(getRowNumber(row))
                .rowReference(getRowReference(row))
                .errorMessage(result.errorMessage())
                .originalValues(result.originalValues() != null ? result.originalValues() : Collections.emptyMap())
                .newValues(result.newValues() != null ? result.newValues() : Collections.emptyMap())
                .rowData(toRowDataMap(row))
                .build();
    }

    /**
     * Loads validated rows from the working file stored at {@link BulkOperation#getWorkingFileStorageKey()}.
     * Returns an empty list if no working file key or fetch/parse fails.
     */
    protected List<Map<String, Object>> loadValidRowsFromWorkingFile(BulkOperation bulkOperation) {
        String workingKey = bulkOperation.getWorkingFileStorageKey();
        if (ValidationUtils.isNullOrEmpty(workingKey)) {
            return List.of();
        }
        try (InputStream inputStream = fileStorageService.fetchFile(workingKey)) {
            return WorkingFileCsvUtils.parseWorkingFileCsv(inputStream);
        } catch (Exception e) {
            log.warn(FAILED_TO_LOAD_WORKING_FILE, bulkOperation.getOperationIdentifier(), e.getMessage());
            return List.of();
        }
    }

    /** Extracts row number from row map. Subclasses may override if using a different key. */
    protected int getRowNumber(Map<String, Object> row) {
        Object v = row != null ? row.get(RowDataKeys.ROW_NUMBER) : null;
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Converts row to map for report. Subclasses define column keys; report layout expects matching keys.
     */
    protected abstract Map<String, String> toRowDataMap(Map<String, Object> row);

    /**
     * Entity/reference id for the row. Subclasses extract from row map using domain-specific keys.
     */
    protected abstract String getRowReference(Map<String, Object> row);
}
