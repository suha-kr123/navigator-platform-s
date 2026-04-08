package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationCsvValidationException;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationNotFoundException;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationCsvValidatorRegistry;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationWorkingFileSaver;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkValidationServiceImplTest {

    @Mock
    private BulkOperationCsvValidatorRegistry validatorRegistry;

    @Mock
    private BulkOperationExceptionFactory bulkOperationExceptionFactory;

    @Mock
    private BulkOperationRepository bulkOperationRepository;

    @Mock
    private BulkOperationReportService reportService;

    @Mock
    private BulkOperationWorkingFileSaver workingFileSaver;

    @Mock
    private BulkOperationFileStorageService fileStorageService;

    @Mock
    private BulkOperationProcessingPersistence persistence;

    @Mock
    private EntityManager entityManager;

    @Mock
    private BulkOperationCsvValidator validator;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private BulkValidationServiceImpl bulkValidationService;

    @Test
    void validateAndUpdateOperation_notFound_throws() {
        UUID id = UUID.randomUUID();
        BulkOperation in = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.UPLOADED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.empty());
        when(bulkOperationExceptionFactory.bulkOperationNotFoundException(id))
                .thenReturn(new BulkOperationNotFoundException("nf"));

        assertThrows(BulkOperationNotFoundException.class,
                () -> bulkValidationService.validateAndUpdateOperation(in, multipartFile));
    }

    @Test
    void validateAndUpdateOperation_csvValidationException_rethrows() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        BulkOperationCsvValidationException ex = new BulkOperationCsvValidationException("bad");
        when(validator.validateCsv(multipartFile)).thenThrow(ex);

        assertSame(ex, assertThrows(BulkOperationCsvValidationException.class,
                () -> bulkValidationService.validateAndUpdateOperation(in, multipartFile)));
    }

    @Test
    void validateAndUpdateOperation_genericParseFailure_wraps() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        when(validator.validateCsv(multipartFile)).thenThrow(new RuntimeException("parse"));
        when(bulkOperationExceptionFactory.bulkOperationCsvValidationFileParseFailedException())
                .thenReturn(new BulkOperationCsvValidationException("parse failed"));

        assertThrows(BulkOperationCsvValidationException.class,
                () -> bulkValidationService.validateAndUpdateOperation(in, multipartFile));
    }

    @Test
    void validateAndUpdateOperation_invalid_setsFailedAndSchedulesReport() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError err = CsvValidationError.builder()
                .rowNumber(3)
                .errorCode("E")
                .errorMessage("bad row")
                .rowReference("L1")
                .build();
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(false)
                .errors(List.of(err))
                .validRowCount(0)
                .errorRowCount(1)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(fileStorageService.saveValidationErrors(eq(id), anyList())).thenReturn("err-key");
        when(reportService.buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList())).thenReturn("rep");
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        assertEquals(BulkOperationStatus.VALIDATION_FAILED, in.getStatus());
        assertEquals("bad row", in.getErrorMessage());
        verify(fileStorageService).saveValidationErrors(eq(id), anyList());
        verify(persistence, atLeastOnce()).persistValidationOutcome(any(BulkOperation.class));
        verify(reportService).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());
        verify(persistence).persistReportKey(eq(id), eq("rep"));
        verify(fileStorageService).deleteFile("err-key");
        verify(persistence).clearValidationErrorsStorageKey(id);
        verify(entityManager, atLeastOnce()).detach(any(BulkOperation.class));
    }

    @Test
    void validateAndUpdateOperation_validWithRows_savesWorkingFile() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        List<Map<String, Object>> validRows = List.of(
                Map.of(RowDataKeys.ROW_NUMBER, 1, "lead_identifier", "L1", "reason_code", "R1")
        );
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(true)
                .errors(List.of())
                .validRows(validRows)
                .validRowCount(1)
                .errorRowCount(0)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(workingFileSaver.saveWorkingFileInNewTransaction(anyString(), eq(id))).thenReturn("work-key");

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        assertEquals(BulkOperationStatus.VALIDATED, in.getStatus());
        assertEquals("work-key", in.getWorkingFileStorageKey());
        verify(workingFileSaver).saveWorkingFileInNewTransaction(anyString(), eq(id));
        verify(persistence).persistValidationOutcome(any(BulkOperation.class));
    }

    @Test
    void validateAndUpdateOperation_invalid_emptyErrors_usesSyntheticError() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(false)
                .errors(List.of())
                .validRowCount(0)
                .errorRowCount(0)
                .totalRows(0)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(reportService.buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList())).thenReturn("rep");
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        assertEquals("Validation failed", in.getErrorMessage());
        verify(reportService).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());
    }

    @Test
    void validateAndUpdateOperation_validWithNonEmptyErrors_savesErrorsAndReport() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError warn = CsvValidationError.builder().rowNumber(1).errorCode("W").errorMessage("warn").build();
        List<Map<String, Object>> validRows = List.of(
                Map.of(RowDataKeys.ROW_NUMBER, 1, "lead_identifier", "L1", "reason_code", "R1"));
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(true)
                .errors(List.of(warn))
                .validRows(validRows)
                .validRowCount(1)
                .errorRowCount(0)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(workingFileSaver.saveWorkingFileInNewTransaction(anyString(), eq(id))).thenReturn("wk");
        when(fileStorageService.saveValidationErrors(eq(id), anyList())).thenReturn("ek");
        when(reportService.buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList())).thenReturn("rep");
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        verify(fileStorageService).saveValidationErrors(eq(id), anyList());
        verify(reportService).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());
    }

    @Test
    void validateAndUpdateOperation_dryRun_setsValidatedDryRun() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED_DRY_RUN);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(true)
                .errors(List.of())
                .validRows(List.of())
                .validRowCount(0)
                .errorRowCount(0)
                .totalRows(0)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        assertEquals(BulkOperationStatus.VALIDATED_DRY_RUN, in.getStatus());
    }

    @Test
    void validateAndUpdateOperation_totalRowsInferredFromValidRowsWhenCountsZero() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        List<Map<String, Object>> validRows = List.of(
                Map.of(RowDataKeys.ROW_NUMBER, 1, "lead_identifier", "L1", "reason_code", "R1"));
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(true)
                .errors(List.of())
                .validRows(validRows)
                .validRowCount(0)
                .errorRowCount(0)
                .totalRows(null)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(workingFileSaver.saveWorkingFileInNewTransaction(anyString(), eq(id))).thenReturn("wk");

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        assertEquals(1, in.getTotalRows());
    }

    @Test
    void validateAndUpdateOperation_totalRowsFromErrorListWhenCountsZero() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError e1 = CsvValidationError.builder().rowNumber(1).errorCode("A").errorMessage("a").build();
        CsvValidationError e2 = CsvValidationError.builder().rowNumber(2).errorCode("B").errorMessage("b").build();
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(true)
                .errors(List.of(e1, e2))
                .validRows(List.of())
                .validRowCount(0)
                .errorRowCount(0)
                .totalRows(null)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        assertEquals(2, in.getTotalRows());
    }

    @Test
    void generateValidationReportAfterCommit_operationMissing_skipsPersist() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError err = CsvValidationError.builder().rowNumber(1).errorCode("E").errorMessage("e").build();
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(false)
                .errors(List.of(err))
                .validRowCount(0)
                .errorRowCount(1)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(fileStorageService.saveValidationErrors(eq(id), anyList())).thenReturn("ek");
        when(bulkOperationRepository.findByOperationIdentifier(id))
                .thenReturn(Optional.of(in))
                .thenReturn(Optional.empty());

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        verify(reportService, never()).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());
    }

    @Test
    void generateValidationReport_retrySucceedsAfterFirstFailure() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError err = CsvValidationError.builder().rowNumber(1).errorCode("E").errorMessage("e").build();
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(false)
                .errors(List.of(err))
                .validRowCount(0)
                .errorRowCount(1)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(fileStorageService.saveValidationErrors(eq(id), anyList())).thenReturn("ek");
        when(reportService.buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList()))
                .thenThrow(new RuntimeException("first"))
                .thenReturn("rep");

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        verify(reportService, times(2)).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());
        verify(persistence).persistReportKey(id, "rep");
    }

    @Test
    void generateValidationReport_bothAttemptsFail_persistReportFailure() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError err = CsvValidationError.builder().rowNumber(1).errorCode("E").errorMessage("e").build();
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(false)
                .errors(List.of(err))
                .validRowCount(0)
                .errorRowCount(1)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(fileStorageService.saveValidationErrors(eq(id), anyList())).thenReturn("ek");
        when(reportService.buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList()))
                .thenThrow(new RuntimeException("a"))
                .thenThrow(new RuntimeException("b"));

        bulkValidationService.validateAndUpdateOperation(in, multipartFile);

        verify(persistence).persistReportFailure(eq(id), anyString());
    }

    @Test
    void scheduleReportAfterCommit_whenSyncActive_defersUntilAfterCommit() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation in = stubOperation(id, BulkOperationStatus.UPLOADED);
        when(validatorRegistry.getValidator(BulkOperationType.DROPOFF)).thenReturn(validator);
        CsvValidationError err = CsvValidationError.builder().rowNumber(1).errorCode("E").errorMessage("e").build();
        CsvValidationResult result = CsvValidationResult.builder()
                .isValid(false)
                .errors(List.of(err))
                .validRowCount(0)
                .errorRowCount(1)
                .totalRows(1)
                .build();
        when(validator.validateCsv(multipartFile)).thenReturn(result);
        when(fileStorageService.saveValidationErrors(eq(id), anyList())).thenReturn("ek");
        when(reportService.buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList())).thenReturn("rep");

        AtomicReference<TransactionSynchronization> syncRef = new AtomicReference<>();
        try (MockedStatic<TransactionSynchronizationManager> tx = mockStatic(TransactionSynchronizationManager.class)) {
            tx.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
            tx.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        syncRef.set(invocation.getArgument(0));
                        return null;
                    });
            when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(in));

            bulkValidationService.validateAndUpdateOperation(in, multipartFile);

            verify(reportService, never()).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());

            syncRef.get().afterCommit();
        }

        verify(reportService).buildAndSaveUnifiedReport(any(), anyList(), anyList(), anyList());
    }

    private static BulkOperation stubOperation(UUID id, BulkOperationStatus status) {
        return BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(status)
                .build();
    }
}
