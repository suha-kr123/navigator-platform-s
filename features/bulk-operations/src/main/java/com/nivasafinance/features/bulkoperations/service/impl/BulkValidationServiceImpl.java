package com.nivasafinance.features.bulkoperations.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.utils.WorkingFileCsvUtils;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationCsvValidatorRegistry;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkValidationService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkValidationServiceImpl implements BulkValidationService {

    private static final String VALIDATION_FAILED_FOR_OPERATION_TYPE = "Validation failed for operation type: {}";

    private final BulkOperationCsvValidatorRegistry validatorRegistry;
    private final BulkOperationExceptionFactory bulkOperationExceptionFactory;
    private final BulkOperationRepository bulkOperationRepository;
    private final BulkOperationReportService reportService;
    private final BulkOperationFileStorageService fileStorageService;

    @Override
    public void validateAndUpdateOperation(BulkOperation bulkOperation, MultipartFile file) {
        BulkOperationCsvValidator validator = validatorRegistry.getValidator(bulkOperation.getOperationType());
        CsvValidationResult validationResult;
        try {
            validationResult = validator.validateCsv(file);
        } catch (Exception e) {
            log.error(VALIDATION_FAILED_FOR_OPERATION_TYPE, bulkOperation.getOperationType(), e.getMessage(), e);
            throw bulkOperationExceptionFactory.bulkOperationCsvValidationFileParseFailedException();
        }

        applyValidationOutcomes(bulkOperation, validationResult);

        if (!validationResult.isValid()) {
            bulkOperation.setStatus(BulkOperationStatus.VALIDATION_FAILED);
            bulkOperation.setValidationCompletedAt(LocalDateTime.now());
            if (!ValidationUtils.isNullOrEmpty(validationResult.getErrors())) {
                String firstError = validationResult.getErrors().get(0).getErrorMessage();
                bulkOperation.setErrorMessage(firstError);
                String reportKey = reportService.buildAndSaveUnifiedReport(
                        bulkOperation, validationResult.getErrors(), List.of(), List.of());
                bulkOperation.setSummaryStorageKey(reportKey);
            }
            bulkOperationRepository.save(bulkOperation);
            return;
        }
        // Non-dry-run with valid rows: BulkOperationValidationListener publishes to processing queue
    }

    private void applyValidationOutcomes(BulkOperation bulkOperation, CsvValidationResult validationResult) {
        int validCount = validationResult.getValidRowCount() != null ? validationResult.getValidRowCount() : 0;
        int errorCount = validationResult.getErrorRowCount() != null ? validationResult.getErrorRowCount() : 0;
        Integer totalRows = validationResult.getTotalRows();
        if (totalRows == null) {
            totalRows = validCount + errorCount;
            if (totalRows == 0 && !ValidationUtils.isEmpty(validationResult.getErrors())) {
                totalRows = validationResult.getErrors().size();
            }
            if (totalRows == 0 && !ValidationUtils.isEmpty(validationResult.getValidRows())) {
                totalRows = validationResult.getValidRows().size();
            }
        }
        bulkOperation.setTotalRows(totalRows);
        bulkOperation.setValidRows(validCount);
        bulkOperation.setInvalidRows(errorCount);
        bulkOperation.setStatus(BulkOperationStatus.VALIDATED);
        bulkOperation.setValidationCompletedAt(LocalDateTime.now());

        if (!ValidationUtils.isNullOrEmpty(validationResult.getValidRows())) {
            String csvContent = WorkingFileCsvUtils.generateWorkingFileCsv(
                    bulkOperation.getOperationType(), validationResult.getValidRows());
            String workingKey = fileStorageService.saveCsvContent(
                    csvContent,
                    bulkOperation.getOperationIdentifier(),
                    WorkingFileCsvUtils.getWorkingFileName());
            bulkOperation.setWorkingFileStorageKey(workingKey);
        }

        bulkOperationRepository.save(bulkOperation);
    }
}