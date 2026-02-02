package com.nivasafinance.features.bulkoperations.service;

import java.util.UUID;

/**
 * Records bulk operation processing failures in a separate transaction (REQUIRES_NEW)
 * so the caller's transaction is not marked rollback-only.
 */
public interface BulkOperationFailureRecorder {

    void recordProcessingFailure(UUID operationId, Exception cause);
}
