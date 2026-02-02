package com.nivasafinance.features.bulkoperations.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.utils.CsvReportGenerator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationReportServiceImpl implements BulkOperationReportService {

    private static final String REPORT_FILE_NAME = "bulk_operation_report.csv";
    private static final String SAVED_UNIFIED_BULK_OPERATION_REPORT_FOR_OPERATION = "Saved unified bulk operation report for operation {} to storage: {}";

    private final BulkOperationFileStorageService fileStorageService;
    private final Map<BulkOperationType, BulkOperationReportLayout> reportLayouts;
    private final BulkOperationExceptionFactory exceptionFactory;

    @Override
    public String buildAndSaveUnifiedReport(
            BulkOperation bulkOperation,
            List<CsvValidationError> validationErrors,
            List<CsvReportRow> successRows,
            List<CsvReportRow> failedRows) {
        BulkOperationReportLayout layout = reportLayouts.get(bulkOperation.getOperationType());
        if (layout == null) {
            throw exceptionFactory.bulkOperationReportLayoutNotFoundException(bulkOperation.getOperationType());
        }
        List<CsvValidationError> errors = validationErrors != null ? validationErrors : List.of();
        String csvContent = CsvReportGenerator.generateUnifiedReport(
                bulkOperation, errors, successRows, failedRows, layout);
        UUID operationId = bulkOperation.getOperationIdentifier();
        String storageKey = fileStorageService.saveCsvContent(csvContent, operationId, REPORT_FILE_NAME);
        log.info(SAVED_UNIFIED_BULK_OPERATION_REPORT_FOR_OPERATION, operationId, storageKey);
        return storageKey;
    }
}
