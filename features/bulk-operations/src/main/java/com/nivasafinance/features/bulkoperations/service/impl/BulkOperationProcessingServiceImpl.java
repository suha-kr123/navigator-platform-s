package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.OperationProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationProcessor;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationProcessorRegistry;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationFailureRecorder;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationProcessingServiceImpl implements BulkOperationProcessingService {

	private static final String ERROR_MESSAGE_TEMPLATE = "Bulk operation processing failed: %s";

	private final BulkOperationProcessorRegistry processorRegistry;
	private final BulkOperationRepository bulkOperationRepository;
	private final BulkOperationExceptionFactory exceptionFactory;
	private final BulkOperationFailureRecorder failureRecorder;
	private final BulkOperationProcessingPersistence persistence;
	private final BulkOperationReportService reportService;
	private final BulkOperationFileStorageService fileStorageService;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void process(BulkOperation bulkOperation) {
		BulkOperation operation = bulkOperationRepository
				.findByOperationIdentifier(bulkOperation.getOperationIdentifier())
				.orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(bulkOperation.getOperationIdentifier()));

		persistence.markProcessingStarted(operation.getOperationIdentifier());

		try {
			BulkOperationProcessor processor = processorRegistry.getProcessor(operation.getOperationType());
			OperationProcessingResult result = processor.process(operation);

			persistence.persistSuccess(operation.getOperationIdentifier(), result);

			UUID operationId = operation.getOperationIdentifier();
			List<CsvReportRow> successRows = result.successRows();
			List<CsvReportRow> failedRows = result.failedRows();
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					generateReportAfterCommit(operationId, successRows, failedRows);
				}
			});
		} catch (Exception ex) {
			log.error(ERROR_MESSAGE_TEMPLATE, operation.getId(), ex);
			failureRecorder.recordProcessingFailure(operation.getOperationIdentifier(), ex);
		}
	}

	private void generateReportAfterCommit(UUID operationId, List<CsvReportRow> successRows, List<CsvReportRow> failedRows) {
		if (successRows.isEmpty() && failedRows.isEmpty()) {
			return;
		}
		BulkOperation operation = bulkOperationRepository.findByOperationIdentifier(operationId).orElse(null);
		if (operation == null) return;
		List<CsvValidationError> validationErrors = fileStorageService.fetchValidationErrors(operation.getValidationErrorsStorageKey());
		try {
			String storageKey = reportService.buildAndSaveUnifiedReport(operation, validationErrors, successRows, failedRows);
			persistence.persistReportKey(operationId, storageKey);
			deleteValidationErrorsFileAndClearKey(operationId, operation.getValidationErrorsStorageKey());
		} catch (Exception e) {
			log.warn("Failed to generate report after commit for operation {}: {}", operationId, e.getMessage(), e);
			try {
				String storageKey = reportService.buildAndSaveUnifiedReport(operation, validationErrors, successRows, failedRows);
				persistence.persistReportKey(operationId, storageKey);
				deleteValidationErrorsFileAndClearKey(operationId, operation.getValidationErrorsStorageKey());
			} catch (Exception retryEx) {
				log.error("Report generation failed after retry for operation {}: {}", operationId, retryEx.getMessage(), retryEx);
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

	@Override
	public void recordProcessingFailure(UUID operationId, Exception cause) {
		failureRecorder.recordProcessingFailure(operationId, cause);
	}
}
