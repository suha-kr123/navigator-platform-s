package com.nivasafinance.features.bulkoperations.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "bulk.operations")
public class BulkOperationProcessingProperties {

    /** Number of rows to process per batch; progress (currentBatch, totalBatches, processedRows) is updated after each batch. */
    private int batchSize = 100;

    /** Max number of bulk operations that may run concurrently; prevents thread-pool exhaustion. */
    private int maxConcurrentProcessing = 2;

    public void setBatchSize(int batchSize) {
        this.batchSize = Math.max(1, batchSize);
    }

    public void setMaxConcurrentProcessing(int maxConcurrentProcessing) {
        this.maxConcurrentProcessing = Math.max(1, maxConcurrentProcessing);
    }
}
