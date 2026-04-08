package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationProcessingQueuePublisherImplTest {

    @Mock
    private MessagePublisherFactory messagePublisherFactory;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private BulkOperationProcessingQueuePublisherImpl publisher;

    @Test
    void publishToProcessingQueueAsync_success() {
        UUID id = UUID.randomUUID();
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);

        publisher.publishToProcessingQueueAsync(id);

        verify(messagePublisher).publish(
                eq(QueueType.BULK_OPERATION_PROCESSING),
                eq(id.toString()),
                argThat(m -> id.toString().equals(m.get("operationId"))));
    }

    @Test
    void publishToProcessingQueueAsync_failure_swallowed() {
        UUID id = UUID.randomUUID();
        when(messagePublisherFactory.getPublisher()).thenThrow(new RuntimeException("sqs"));

        publisher.publishToProcessingQueueAsync(id);

        verify(messagePublisherFactory).getPublisher();
    }
}
