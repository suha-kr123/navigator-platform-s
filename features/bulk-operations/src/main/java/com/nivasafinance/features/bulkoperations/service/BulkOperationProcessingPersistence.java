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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markProcessingStarted(UUID operationId) {
		BulkOperation operation = bulkOperationRepository
				.findByOperationIdentifier(operationId)
				.orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
		operation.setStatus(BulkOperationStatus.PROCESSING_IN_PROGRESS);
		operation.setProcessingStartedAt(LocalDateTime.now());
		bulkOperationRepository.save(operation);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void persistSuccess(UUID operationId, OperationProcessingResult result) {
		BulkOperation operation = bulkOperationRepository
				.findByOperationIdentifier(operationId)
				.orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
		operation.setStatus(result.finalStatus());
		operation.setTotalRows(result.totalRows());
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
