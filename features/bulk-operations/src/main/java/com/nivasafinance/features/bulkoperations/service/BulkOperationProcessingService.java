package com.nivasafinance.features.bulkoperations.service;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;

import java.util.UUID;

public interface BulkOperationProcessingService {

    void process(BulkOperation bulkOperation);

    /**
     * Records a processing failure (retry count, status FAILED or VALIDATED, error message).
     * Runs in its own transaction so the caller's transaction is not marked rollback-only on failure.
     */
    void recordProcessingFailure(UUID operationId, Exception cause);
}
