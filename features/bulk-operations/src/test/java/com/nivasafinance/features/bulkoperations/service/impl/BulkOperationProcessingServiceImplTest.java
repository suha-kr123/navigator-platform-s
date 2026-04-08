package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.OperationProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationNotFoundException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationProcessor;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationProcessorRegistry;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationFailureRecorder;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationProcessingServiceImplTest {

    @Mock
    private BulkOperationProcessorRegistry processorRegistry;

    @Mock
    private BulkOperationRepository bulkOperationRepository;

    @Mock
    private BulkOperationExceptionFactory exceptionFactory;

    @Mock
    private BulkOperationFailureRecorder failureRecorder;

    @Mock
    private BulkOperationProcessingPersistence persistence;

    @Mock
    private BulkOperationReportService reportService;

    @Mock
    private BulkOperationFileStorageService fileStorageService;

    @Mock
    private BulkOperationProcessor processor;

    @InjectMocks
    private BulkOperationProcessingServiceImpl bulkOperationProcessingService;

    @Test
    void process_operationNotFound_throws() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.empty());
        when(exceptionFactory.bulkOperationNotFoundException(id)).thenReturn(new BulkOperationNotFoundException("nf"));

        assertThrows(BulkOperationNotFoundException.class, () -> bulkOperationProcessingService.process(in));
    }

    @Test
    void process_processorThrows_recordsFailure() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenThrow(new RuntimeException("proc fail"));

        bulkOperationProcessingService.process(in);

        verify(persistence).markProcessingStarted(id);
        verify(failureRecorder).recordProcessingFailure(eq(id), any(RuntimeException.class));
    }

    @Test
    void process_success_registersAfterCommitAndPersistsReport() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .validationErrorsStorageKey("vk")
                .build();
        CsvReportRow row = CsvReportRow.builder()
                .rowNumber(1)
                .rowReference("r1")
                .rowData(Map.of("lead_identifier", "L1"))
                .build();
        OperationProcessingResult result = new OperationProcessingResult(
                BulkOperationStatus.COMPLETED,
                1,
                1,
                0,
                null,
                null,
                List.of(row),
                List.of());

        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenReturn(result);
        when(fileStorageService.fetchValidationErrors("vk")).thenReturn(List.of());
        when(reportService.buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), anyList())).thenReturn("rk");

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        syncRef.set(inv.getArgument(0));
                        return null;
                    });

            bulkOperationProcessingService.process(in);

            assertNotNull(syncRef.get());
            syncRef.get().afterCommit();
        }

        verify(persistence).persistSuccess(eq(id), eq(result));
        verify(reportService).buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), anyList());
        verify(persistence).persistReportKey(id, "rk");
    }

    @Test
    void process_afterCommitEmptyRows_skipsReport() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .build();
        OperationProcessingResult result = new OperationProcessingResult(
                BulkOperationStatus.COMPLETED,
                0,
                0,
                0,
                null,
                null,
                List.of(),
                List.of());

        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenReturn(result);

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        syncRef.set(inv.getArgument(0));
                        return null;
                    });

            bulkOperationProcessingService.process(in);
            syncRef.get().afterCommit();
        }

        verify(reportService, never()).buildAndSaveUnifiedReport(any(), any(), any(), any());
    }

    @Test
    void process_afterCommit_operationGone_skipsReport() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .validationErrorsStorageKey("vk")
                .build();
        CsvReportRow row = CsvReportRow.builder().rowNumber(1).rowReference("r").rowData(Map.of("lead_identifier", "L")).build();
        OperationProcessingResult result = new OperationProcessingResult(
                BulkOperationStatus.COMPLETED, 1, 1, 0, null, null, List.of(row), List.of());

        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in)).thenReturn(Optional.empty());
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenReturn(result);

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        syncRef.set(inv.getArgument(0));
                        return null;
                    });

            bulkOperationProcessingService.process(in);
            syncRef.get().afterCommit();
        }

        verify(reportService, never()).buildAndSaveUnifiedReport(any(), any(), any(), any());
        verify(fileStorageService, never()).fetchValidationErrors(any());
    }

    @Test
    void process_afterCommit_reportFailsTwice_persistReportFailure() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .validationErrorsStorageKey("vk")
                .build();
        CsvReportRow row = CsvReportRow.builder().rowNumber(1).rowReference("r").rowData(Map.of("lead_identifier", "L")).build();
        OperationProcessingResult result = new OperationProcessingResult(
                BulkOperationStatus.COMPLETED, 1, 1, 0, null, null, List.of(row), List.of());

        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenReturn(result);
        when(fileStorageService.fetchValidationErrors("vk")).thenReturn(List.of());
        when(reportService.buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), anyList()))
                .thenThrow(new RuntimeException("a"))
                .thenThrow(new RuntimeException("b"));

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        syncRef.set(inv.getArgument(0));
                        return null;
                    });

            bulkOperationProcessingService.process(in);
            syncRef.get().afterCommit();
        }

        verify(persistence).persistReportFailure(eq(id), anyString());
    }

    @Test
    void process_afterCommit_firstReportFails_retrySucceeds_andClearsValidationKey() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .validationErrorsStorageKey("vk")
                .build();
        CsvReportRow row = CsvReportRow.builder().rowNumber(1).rowReference("r").rowData(Map.of("lead_identifier", "L")).build();
        OperationProcessingResult result = new OperationProcessingResult(
                BulkOperationStatus.COMPLETED, 1, 1, 0, null, null, List.of(row), List.of());

        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenReturn(result);
        when(fileStorageService.fetchValidationErrors("vk")).thenReturn(List.of());
        when(reportService.buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), anyList()))
                .thenThrow(new RuntimeException("transient"))
                .thenReturn("rk");

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        syncRef.set(inv.getArgument(0));
                        return null;
                    });

            bulkOperationProcessingService.process(in);
            syncRef.get().afterCommit();
        }

        verify(reportService, times(2)).buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), anyList());
        verify(persistence).persistReportKey(id, "rk");
        verify(fileStorageService).deleteFile("vk");
        verify(persistence).clearValidationErrorsStorageKey(id);
    }

    @Test
    void process_afterCommit_failedRowsOnly_stillGeneratesReport() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .validationErrorsStorageKey(null)
                .build();
        CsvReportRow failed = CsvReportRow.builder().rowNumber(2).rowReference("f").rowData(Map.of("lead_identifier", "X")).build();
        OperationProcessingResult result = new OperationProcessingResult(
                BulkOperationStatus.PARTIALLY_COMPLETED, 1, 0, 1, null, null, List.of(), List.of(failed));

        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(processorRegistry.getProcessor(BulkOperationType.DROPOFF)).thenReturn(processor);
        when(processor.process(in)).thenReturn(result);
        when(fileStorageService.fetchValidationErrors(null)).thenReturn(List.of());
        when(reportService.buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), anyList())).thenReturn("rk");

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        syncRef.set(inv.getArgument(0));
                        return null;
                    });

            bulkOperationProcessingService.process(in);
            syncRef.get().afterCommit();
        }

        verify(reportService).buildAndSaveUnifiedReport(eq(in), anyList(), anyList(), argThat(list -> list.size() == 1));
        verify(persistence).persistReportKey(id, "rk");
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    void recordProcessingFailure_delegates() {
        UUID id = UUID.randomUUID();
        RuntimeException ex = new RuntimeException("x");
        bulkOperationProcessingService.recordProcessingFailure(id, ex);
        verify(failureRecorder).recordProcessingFailure(id, ex);
    }
}
