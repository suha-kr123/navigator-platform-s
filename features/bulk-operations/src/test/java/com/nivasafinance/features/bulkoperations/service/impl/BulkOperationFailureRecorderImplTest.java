package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationFailureRecorderImplTest {

    @Mock
    private BulkOperationRepository bulkOperationRepository;

    @Mock
    private BulkOperationExceptionFactory exceptionFactory;

    @InjectMocks
    private BulkOperationFailureRecorderImpl failureRecorder;

    @Test
    void recordProcessingFailure_missingOperation_logsOnly() {
        UUID id = UUID.randomUUID();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.empty());

        failureRecorder.recordProcessingFailure(id, new RuntimeException("x"));

        verify(bulkOperationRepository, never()).save(any());
    }

    @Test
    void recordProcessingFailure_underMaxRetries_setsValidated() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .retryCount(0)
                .maxRetryCount(3)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));

        failureRecorder.recordProcessingFailure(id, new IllegalStateException("boom"));

        assertEquals(1, op.getRetryCount());
        assertEquals(BulkOperationStatus.VALIDATED, op.getStatus());
        assertNotNull(op.getLastRetryAt());
        verify(bulkOperationRepository).save(op);
    }

    @Test
    void recordProcessingFailure_reachesMaxRetries_setsFailed() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .retryCount(2)
                .maxRetryCount(3)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(exceptionFactory.createProcessingFailedAfterRetriesMessage(eq(3), eq(3), anyString()))
                .thenReturn("final msg");

        failureRecorder.recordProcessingFailure(id, new RuntimeException("last"));

        assertEquals(3, op.getRetryCount());
        assertEquals(BulkOperationStatus.FAILED, op.getStatus());
        assertEquals("final msg", op.getErrorMessage());
        assertNotNull(op.getProcessingCompletedAt());
        verify(bulkOperationRepository).save(op);
    }

    @Test
    void recordProcessingFailure_nullRetryCounts_useDefaults() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .retryCount(null)
                .maxRetryCount(null)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));

        failureRecorder.recordProcessingFailure(id, new RuntimeException("e"));

        assertEquals(1, op.getRetryCount());
        assertEquals(BulkOperationStatus.VALIDATED, op.getStatus());
    }

    @Test
    void recordProcessingFailure_nullCause_usesEmptyDetail() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .retryCount(2)
                .maxRetryCount(3)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(exceptionFactory.createProcessingFailedAfterRetriesMessage(eq(3), eq(3), eq("")))
                .thenReturn("done");

        failureRecorder.recordProcessingFailure(id, null);

        assertEquals(BulkOperationStatus.FAILED, op.getStatus());
    }
}
