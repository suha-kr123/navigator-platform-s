package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingQueuePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationProcessingQueuePublisherImpl implements BulkOperationProcessingQueuePublisher {

    private static final String LOG_PUBLISH_FAILED = "Failed to publish bulk operation {} to processing queue: {}";

    private final MessagePublisherFactory messagePublisherFactory;

    @Override
    @Async("bulkOperationTaskExecutor")
    public void publishToProcessingQueueAsync(UUID operationId) {
        try {
            log.info("Publishing bulk operation {} to {} queue (async)", operationId, QueueType.BULK_OPERATION_PROCESSING);
            messagePublisherFactory.getPublisher().publish(
                    QueueType.BULK_OPERATION_PROCESSING,
                    operationId.toString(),
                    Map.of("operationId", operationId.toString()));
            log.info("Published bulk operation {} to processing queue", operationId);
        } catch (Exception ex) {
            log.warn(LOG_PUBLISH_FAILED, operationId, ex.getMessage());
        }
    }
}
