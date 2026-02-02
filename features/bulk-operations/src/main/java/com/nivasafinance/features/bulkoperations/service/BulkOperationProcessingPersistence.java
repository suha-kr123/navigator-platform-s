package com.nivasafinance.features.bulkoperations.service;

import com.nivasafinance.features.bulkoperations.common.dto.OperationProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persists bulk operation state in separate transactions so commit-phase failures
 * (e.g. Javers) do not mark the main processing transaction rollback-only.
 */
@Component
public class BulkOperationProcessingPersistence {

	private final BulkOperationRepository bulkOperationRepository;
	private final BulkOperationExceptionFactory exceptionFactory;

	public BulkOperationProcessingPersistence(BulkOperationRepository bulkOperationRepository,
			BulkOperationExceptionFactory exceptionFactory) {
		this.bulkOperationRepository = bulkOperationRepository;
		this.exceptionFactory = exceptionFactory;
	}

	/**
	 * Persists validation outcome (status, counts, working file key, validation errors storage key, etc.)
	 * in a separate transaction so commit-phase failures (e.g. Javers, auditing) do not mark the
	 * validation transaction rollback-only. Nothing is written to metadata.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistValidationOutcome(BulkOperation operation) {
		BulkOperation entity = bulkOperationRepository
				.findByOperationIdentifier(operation.getOperationIdentifier())
				.orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operation.getOperationIdentifier()));
		entity.setStatus(operation.getStatus());
		entity.setTotalRows(operation.getTotalRows());
		entity.setValidRows(operation.getValidRows());
		entity.setInvalidRows(operation.getInvalidRows());
		entity.setWorkingFileStorageKey(operation.getWorkingFileStorageKey());
		entity.setValidationErrorsStorageKey(operation.getValidationErrorsStorageKey());
		entity.setValidationCompletedAt(operation.getValidationCompletedAt());
		entity.setErrorMessage(operation.getErrorMessage());
		bulkOperationRepository.save(entity);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markProcessingStarted(UUID operationId) {
		BulkOperation operation = bulkOperationRepository
				.findByOperationIdentifier(operationId)
				.orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
		operation.setStatus(BulkOperationStatus.PROCESSING_IN_PROGRESS);
		operation.setProcessingStartedAt(LocalDateTime.now());
		bulkOperationRepository.save(operation);
	}

	/**
	 * Updates only processing outcome fields (status, success/failed counts, report key, etc.).
	 * Does not overwrite totalRows/validRows/invalidRows (set at validation time).
	 * Does not modify isDryRun so dry-run operations remain marked as dry run after completion.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistSuccess(UUID operationId, OperationProcessingResult result) {
		BulkOperation operation = bulkOperationRepository
				.findByOperationIdentifier(operationId)
				.orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
		if (result.totalRows() > 0) {
			operation.setStatus(result.finalStatus());
		} else if (operation.getValidRows() != null && operation.getValidRows() == 0) {
			operation.setStatus(BulkOperationStatus.VALIDATION_FAILED);
		}
		operation.setProcessedRows(result.totalRows());
		operation.setSuccessfulRows(result.successCount());
		operation.setFailedRows(result.failureCount());
		operation.setSummaryStorageKey(result.summaryStorageKey());
		operation.setErrorMessage(result.errorMessage());
		operation.setProcessingCompletedAt(LocalDateTime.now());
		bulkOperationRepository.save(operation);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistBatchProgress(UUID operationId, int currentBatch, int totalBatches, int processedRows) {
		bulkOperationRepository.findByOperationIdentifier(operationId).ifPresent(op -> {
			op.setCurrentBatch(currentBatch);
			op.setTotalBatches(totalBatches);
			op.setProcessedRows(processedRows);
			bulkOperationRepository.save(op);
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistReportKey(UUID operationId, String summaryStorageKey) {
		bulkOperationRepository.findByOperationIdentifier(operationId).ifPresent(op -> {
			op.setSummaryStorageKey(summaryStorageKey);
			bulkOperationRepository.save(op);
		});
	}

	/** Clears validation errors storage key after report is generated and file is deleted. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void clearValidationErrorsStorageKey(UUID operationId) {
		bulkOperationRepository.findByOperationIdentifier(operationId).ifPresent(op -> {
			op.setValidationErrorsStorageKey(null);
			bulkOperationRepository.save(op);
		});
	}

	/**
	 * Records that report generation failed after commit. Appends to errorMessage so the user sees it.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistReportFailure(UUID operationId, String errorMessage) {
		bulkOperationRepository.findByOperationIdentifier(operationId).ifPresent(op -> {
			String existing = op.getErrorMessage();
			String message = (existing != null && !existing.isBlank())
					? existing + "; Report generation failed: " + errorMessage
					: "Report generation failed: " + errorMessage;
			op.setErrorMessage(message);
			bulkOperationRepository.save(op);
		});
	}
}
