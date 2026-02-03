package com.nivasafinance.features.bulkoperations.service;

import java.util.UUID;

/**
 * Publishes bulk operations to the processing queue.
 * Async implementation runs the publish in a separate thread after the caller returns,
 * avoiding any interference from the validation/scheduling context.
 */
public interface BulkOperationProcessingQueuePublisher {

    /**
     * Publishes the operation to BULK_OPERATION_PROCESSING queue asynchronously.
     * Runs on bulkOperationTaskExecutor so it executes after the caller's flow completes.
     */
    void publishToProcessingQueueAsync(UUID operationId);
}
