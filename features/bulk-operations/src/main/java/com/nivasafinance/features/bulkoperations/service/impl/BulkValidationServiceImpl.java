package com.nivasafinance.features.bulkoperations.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.utils.WorkingFileCsvUtils;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationCsvValidatorRegistry;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationWorkingFileSaver;
import com.nivasafinance.features.bulkoperations.service.BulkValidationService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkValidationServiceImpl implements BulkValidationService {

    private static final String VALIDATION_FAILED_FOR_OPERATION_TYPE = "Validation failed for operation type: {}";
    private static final String FAILED_TO_GENERATE_VALIDATION_REPORT_AFTER_COMMIT = "Failed to generate validation report after commit for operation {}: {}";

    private final BulkOperationCsvValidatorRegistry validatorRegistry;
    private final BulkOperationExceptionFactory bulkOperationExceptionFactory;
    private final BulkOperationRepository bulkOperationRepository;
    private final BulkOperationReportService reportService;
    private final BulkOperationWorkingFileSaver workingFileSaver;
    private final BulkOperationFileStorageService fileStorageService;
    private final BulkOperationProcessingPersistence persistence;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void validateAndUpdateOperation(BulkOperation bulkOperation, MultipartFile file) {
        BulkOperation operation = bulkOperationRepository
                .findByOperationIdentifier(bulkOperation.getOperationIdentifier())
                .orElseThrow(() -> bulkOperationExceptionFactory.bulkOperationNotFoundException(bulkOperation.getOperationIdentifier()));

        BulkOperationCsvValidator validator = validatorRegistry.getValidator(operation.getOperationType());
        CsvValidationResult validationResult;
        try {
            validationResult = validator.validateCsv(file);
        } catch (Exception e) {
            log.error(VALIDATION_FAILED_FOR_OPERATION_TYPE, operation.getOperationType(), e.getMessage(), e);
            throw bulkOperationExceptionFactory.bulkOperationCsvValidationFileParseFailedException();
        }

        applyValidationOutcomes(operation, validationResult);

        List<CsvValidationError> errors = validationResult.getErrors() != null ? validationResult.getErrors() : List.of();

        if (!validationResult.isValid()) {
            operation.setStatus(BulkOperationStatus.VALIDATION_FAILED);
            operation.setValidationCompletedAt(LocalDateTime.now());
            if (!ValidationUtils.isNullOrEmpty(errors)) {
                operation.setErrorMessage(errors.get(0).getErrorMessage());
            } else {
                operation.setErrorMessage("Validation failed");
            }
            List<CsvValidationError> errorsForReport = ValidationUtils.isNullOrEmpty(errors)
                    ? List.of(CsvValidationError.builder()
                            .rowNumber(1)
                            .errorCode("VALIDATION_FAILED")
                            .errorMessage(operation.getErrorMessage())
                            .rowReference("")
                            .build())
                    : errors;
            saveValidationErrorsToStorage(operation, errors);
            persistence.persistValidationOutcome(operation);
            scheduleReportAfterCommit(operation.getOperationIdentifier(), errorsForReport);
            return;
        }

        if (!errors.isEmpty()) {
            List<CsvValidationError> errorsForReport = errors;
            saveValidationErrorsToStorage(operation, errors);
            persistence.persistValidationOutcome(operation);
            scheduleReportAfterCommit(operation.getOperationIdentifier(), errorsForReport);
        }
        // Non-dry-run with valid rows: BulkOperationValidationListener publishes to processing queue
    }

    /**
     * Schedules report generation to run after commit when a transaction is active;
     * otherwise runs it immediately so we never throw from registerSynchronization and leave status stuck.
     */
    private void scheduleReportAfterCommit(UUID operationId, List<CsvValidationError> errorsForReport) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    generateValidationReportAfterCommit(operationId, errorsForReport);
                }
            });
        } else {
            generateValidationReportAfterCommit(operationId, errorsForReport);
        }
    }

    /**
     * Builds and saves the validation report to storage after the validation transaction has committed.
     * Externalize results (step 3) runs strictly after commit so streaming is safe.
     */
    private void generateValidationReportAfterCommit(UUID operationId, List<CsvValidationError> validationErrors) {
        BulkOperation operation = bulkOperationRepository.findByOperationIdentifier(operationId).orElse(null);
        if (operation == null) {
            return;
        }
        List<CsvValidationError> errors = validationErrors != null ? validationErrors : Collections.emptyList();
        try {
            String storageKey = reportService.buildAndSaveUnifiedReport(operation, errors, List.of(), List.of());
            persistence.persistReportKey(operationId, storageKey);
            deleteValidationErrorsFileAndClearKey(operationId, operation.getValidationErrorsStorageKey());
        } catch (Exception e) {
            log.warn(FAILED_TO_GENERATE_VALIDATION_REPORT_AFTER_COMMIT, operationId, e.getMessage(), e);
            try {
                String storageKey = reportService.buildAndSaveUnifiedReport(operation, errors, List.of(), List.of());
                persistence.persistReportKey(operationId, storageKey);
                deleteValidationErrorsFileAndClearKey(operationId, operation.getValidationErrorsStorageKey());
            } catch (Exception retryEx) {
                log.error("Validation report generation failed after retry for operation {}: {}", operationId, retryEx.getMessage(), retryEx);
                persistence.persistReportFailure(operationId, retryEx.getMessage() != null ? retryEx.getMessage() : retryEx.getClass().getSimpleName());
            }
        }
    }

    private void deleteValidationErrorsFileAndClearKey(UUID operationId, String validationErrorsStorageKey) {
        if (validationErrorsStorageKey != null && !validationErrorsStorageKey.isBlank()) {
            fileStorageService.deleteFile(validationErrorsStorageKey);
            persistence.clearValidationErrorsStorageKey(operationId);
        }
    }

    /** Saves validation errors to object storage and sets key on operation. Nothing is stored in metadata. */
    private void saveValidationErrorsToStorage(BulkOperation operation, List<CsvValidationError> errors) {
        if (ValidationUtils.isNullOrEmpty(errors)) return;
        String key = fileStorageService.saveValidationErrors(operation.getOperationIdentifier(), errors);
        operation.setValidationErrorsStorageKey(key);
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
            String workingKey = workingFileSaver.saveWorkingFileInNewTransaction(
                    csvContent,
                    bulkOperation.getOperationIdentifier());
            bulkOperation.setWorkingFileStorageKey(workingKey);
        }

        persistence.persistValidationOutcome(bulkOperation);
    }
}