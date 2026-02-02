package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationFailureRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationFailureRecorderImpl implements BulkOperationFailureRecorder {

	private static final String LOG_PROCESSING_FAILED = "Processing failed for bulk operation {} (retry {}/{}): {}";
	private static final String BULK_OPERATION_NOT_FOUND = "BulkOperation not found: %s";

	private final BulkOperationRepository bulkOperationRepository;
	private final BulkOperationExceptionFactory exceptionFactory;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recordProcessingFailure(UUID operationId, Exception cause) {
		BulkOperation operation = bulkOperationRepository
				.findByOperationIdentifier(operationId)
				.orElse(null);
		if (operation == null) {
			log.warn(BULK_OPERATION_NOT_FOUND, operationId);
			return;
		}

		int retries = operation.getRetryCount() != null ? operation.getRetryCount() : 0;
		int maxRetries = operation.getMaxRetryCount() != null ? operation.getMaxRetryCount() : 3;
		operation.setRetryCount(retries + 1);
		operation.setLastRetryAt(LocalDateTime.now());

		String causeDetail = cause != null ? ExceptionUtils.getRootCauseMessage(cause) : "";
		if (operation.getRetryCount() >= maxRetries) {
			operation.setStatus(BulkOperationStatus.FAILED);
			String errorMessage = exceptionFactory.createProcessingFailedAfterRetriesMessage(
					operation.getRetryCount(),
					maxRetries,
					causeDetail);
			operation.setErrorMessage(errorMessage);
			operation.setProcessingCompletedAt(LocalDateTime.now());
			log.error(LOG_PROCESSING_FAILED, operationId, operation.getRetryCount(), maxRetries, causeDetail);
		} else {
			operation.setStatus(BulkOperationStatus.VALIDATED);
			log.warn(LOG_PROCESSING_FAILED, operationId, operation.getRetryCount(), maxRetries, causeDetail);
		}

		bulkOperationRepository.save(operation);
	}
}
