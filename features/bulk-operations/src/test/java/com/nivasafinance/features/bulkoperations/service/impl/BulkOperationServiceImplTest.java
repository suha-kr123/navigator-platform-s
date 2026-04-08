package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationResponse;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.*;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkOperationServiceImplTest {

    @Mock
    private BulkOperationRepository bulkOperationRepository;

    @Mock
    private BulkOperationExceptionFactory exceptionFactory;

    @Mock
    private BulkOperationFileStorageService fileStorageService;

    @Mock
    private MessagePublisherFactory messagePublisherFactory;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private BulkOperationServiceImpl bulkOperationService;

    @BeforeEach
    void setDuplicateWindow() {
        ReflectionTestUtils.setField(bulkOperationService, "duplicateUploadWindowMinutes", 30);
    }

    @AfterEach
    void clearUser() {
        UserContext.clear();
    }

    @Test
    void uploadCsv_getBytesFails_throwsStorageException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new RuntimeException("io"));
        when(exceptionFactory.storageSaveFailedException(isNull(), any()))
                .thenAnswer(inv -> new BulkOperationStorageException("fail", inv.getArgument(1)));

        assertThrows(BulkOperationStorageException.class,
                () -> bulkOperationService.uploadCsv(file, BulkOperationType.DROPOFF));
    }

    @Test
    void uploadCsv_duplicateWithinWindow_throws() throws Exception {
        UserContext.setUsername("alice");
        byte[] bytes = "a".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(bytes);

        BulkOperation existing = BulkOperation.builder()
                .operationIdentifier(UUID.randomUUID())
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.COMPLETED)
                .build();
        existing.setCreatedAt(LocalDateTime.now());
        when(bulkOperationRepository.findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
                eq("alice"), anyString(), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(existing));
        when(exceptionFactory.duplicateUploadException(anyInt()))
                .thenReturn(new BulkOperationDuplicateUploadException("dup"));

        assertThrows(BulkOperationDuplicateUploadException.class,
                () -> bulkOperationService.uploadCsv(file, BulkOperationType.DROPOFF));
    }

    @Test
    void uploadCsv_success_publishesAndSaves() throws Exception {
        UserContext.setUsername("bob");
        byte[] bytes = "x".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(bytes);
        when(file.getOriginalFilename()).thenReturn("data.csv");
        when(file.getSize()).thenReturn(0L);
        when(file.getContentType()).thenReturn("text/csv");

        when(bulkOperationRepository.findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
                anyString(), anyString(), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of());
        when(bulkOperationRepository.save(any(BulkOperation.class))).thenAnswer(inv -> {
            BulkOperation o = inv.getArgument(0);
            if (o.getOperationIdentifier() == null) {
                o.setOperationIdentifier(UUID.randomUUID());
            }
            return o;
        });
        when(fileStorageService.saveFile(any(), any(UUID.class))).thenReturn("file-key");
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);

        BulkOperationResponse response = bulkOperationService.uploadCsv(file, BulkOperationType.ONHOLD);

        assertNotNull(response.getOperationId());
        assertEquals(BulkOperationStatus.UPLOADED, response.getStatus());
        verify(messagePublisher).publish(eq(QueueType.BULK_OPERATION_VALIDATION), anyString(), anyMap());
        verify(bulkOperationRepository, times(2)).save(any(BulkOperation.class));
    }

    @Test
    void uploadCsv_emptyUsername_usesSystem() throws Exception {
        UserContext.clear();
        byte[] bytes = "y".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(bytes);
        when(file.getOriginalFilename()).thenReturn("d.csv");
        when(file.getSize()).thenReturn((long) bytes.length);
        when(file.getContentType()).thenReturn("text/csv");
        when(bulkOperationRepository.findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
                eq("system"), anyString(), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of());
        when(bulkOperationRepository.save(any(BulkOperation.class))).thenAnswer(inv -> {
            BulkOperation o = inv.getArgument(0);
            if (o.getOperationIdentifier() == null) {
                o.setOperationIdentifier(UUID.randomUUID());
            }
            return o;
        });
        when(fileStorageService.saveFile(any(), any(UUID.class))).thenReturn("k");
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);

        bulkOperationService.uploadCsv(file, BulkOperationType.DROPOFF);

        verify(bulkOperationRepository).findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
                eq("system"), anyString(), any(LocalDateTime.class), anyList());
    }

    @Test
    void uploadCsv_publishFailure_stillCompletes() throws Exception {
        UserContext.setUsername("u");
        byte[] bytes = "z".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(bytes);
        when(file.getOriginalFilename()).thenReturn("e.csv");
        when(file.getSize()).thenReturn((long) bytes.length);
        when(file.getContentType()).thenReturn("text/csv");
        when(bulkOperationRepository.findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
                anyString(), anyString(), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of());
        when(bulkOperationRepository.save(any(BulkOperation.class))).thenAnswer(inv -> {
            BulkOperation o = inv.getArgument(0);
            if (o.getOperationIdentifier() == null) {
                o.setOperationIdentifier(UUID.randomUUID());
            }
            return o;
        });
        when(fileStorageService.saveFile(any(), any(UUID.class))).thenReturn("k2");
        when(messagePublisherFactory.getPublisher()).thenThrow(new RuntimeException("down"));

        assertDoesNotThrow(() -> bulkOperationService.uploadCsv(file, BulkOperationType.DROPOFF));
    }

    @Test
    void uploadCsvDryRun_setsDryRunStatus() throws Exception {
        UserContext.setUsername("u");
        byte[] bytes = "q".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(bytes);
        when(file.getOriginalFilename()).thenReturn("d.csv");
        when(file.getSize()).thenReturn((long) bytes.length);
        when(file.getContentType()).thenReturn("text/csv");
        when(bulkOperationRepository.findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
                anyString(), anyString(), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of());
        when(bulkOperationRepository.save(any(BulkOperation.class))).thenAnswer(inv -> {
            BulkOperation o = inv.getArgument(0);
            if (o.getOperationIdentifier() == null) {
                o.setOperationIdentifier(UUID.randomUUID());
            }
            return o;
        });
        when(fileStorageService.saveFile(any(), any(UUID.class))).thenReturn("k3");
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);

        BulkOperationResponse r = bulkOperationService.uploadCsvDryRun(file, BulkOperationType.DROPOFF);

        assertEquals(BulkOperationStatus.UPLOADED_DRY_RUN, r.getStatus());
    }

    @Test
    void getOperationStatus_found() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.VALIDATED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));

        BulkOperationResponse r = bulkOperationService.getOperationStatus(id);

        assertEquals(id, r.getOperationId());
    }

    @Test
    void getOperationStatus_missing_throws() {
        UUID id = UUID.randomUUID();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.empty());
        when(exceptionFactory.bulkOperationNotFoundException(id))
                .thenReturn(new BulkOperationNotFoundException("missing"));

        assertThrows(BulkOperationNotFoundException.class, () -> bulkOperationService.getOperationStatus(id));
    }

    @Test
    void cancelOperation_notCancellable_throws() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .status(BulkOperationStatus.COMPLETED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(exceptionFactory.bulkOperationNotCancellableException(id))
                .thenReturn(new BulkOperationNotCancellableException("no"));

        assertThrows(BulkOperationNotCancellableException.class, () -> bulkOperationService.cancelOperation(id));
    }

    @Test
    void cancelOperation_success() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .status(BulkOperationStatus.UPLOADED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));

        bulkOperationService.cancelOperation(id);

        assertEquals(BulkOperationStatus.CANCELLED, op.getStatus());
        assertNotNull(op.getCancelledAt());
        verify(bulkOperationRepository).save(op);
    }

    @Test
    void getSummaryReportCsv_noKey_throws() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .summaryStorageKey(null)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(exceptionFactory.bulkOperationReportNotAvailableException(id))
                .thenReturn(new BulkOperationReportNotAvailableException("na"));

        assertThrows(BulkOperationReportNotAvailableException.class,
                () -> bulkOperationService.getSummaryReportCsv(id));
    }

    @Test
    void getSummaryReportCsv_success() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .summaryStorageKey("sk")
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(fileStorageService.fetchFile("sk")).thenReturn(new ByteArrayInputStream("csv".getBytes(StandardCharsets.UTF_8)));

        String csv = bulkOperationService.getSummaryReportCsv(id);

        assertEquals("csv", csv);
    }

    @Test
    void getSummaryReportCsv_fetchFails_wrapsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .summaryStorageKey("sk")
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(fileStorageService.fetchFile("sk")).thenThrow(new RuntimeException("boom"));

        assertThrows(BulkOperationNotFoundException.class,
                () -> bulkOperationService.getSummaryReportCsv(id));
    }

    @Test
    void getOperationHistory_blankUser_returnsEmpty() {
        UserContext.clear();
        PaginatedResponse<BulkOperationResponse> r =
                bulkOperationService.getOperationHistory(new PaginationRequest(0, 10, "createdAt", "DESC"));

        assertTrue(r.getContent().isEmpty());
        assertEquals(0, r.getPagination().getLimit());
        verify(bulkOperationRepository, never()).findByCreatedByOrderByCreatedAtDesc(anyString(), any(Pageable.class));
    }

    @Test
    void getOperationHistory_returnsPage() {
        UserContext.setUsername("sam");
        UUID oid = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(oid)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.COMPLETED)
                .build();
        when(bulkOperationRepository.findByCreatedByOrderByCreatedAtDesc(eq("sam"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(op)));

        PaginatedResponse<BulkOperationResponse> r =
                bulkOperationService.getOperationHistory(new PaginationRequest(0, 10, "createdAt", "DESC"));

        assertEquals(1, r.getContent().size());
        assertEquals(oid, r.getContent().get(0).getOperationId());
    }

    @Test
    void getOperationHistory_clampsNegativeOffsetAndZeroLimit() {
        UserContext.setUsername("sam");
        when(bulkOperationRepository.findByCreatedByOrderByCreatedAtDesc(eq("sam"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        bulkOperationService.getOperationHistory(new PaginationRequest(-5, 0, "createdAt", "DESC"));

        verify(bulkOperationRepository).findByCreatedByOrderByCreatedAtDesc(eq("sam"), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 1));
    }

    @Test
    void executeDryRun_notDryRunFlow_throwsNotFound() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .status(BulkOperationStatus.COMPLETED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(exceptionFactory.bulkOperationNotFoundException(id))
                .thenReturn(new BulkOperationNotFoundException("nf"));

        assertThrows(BulkOperationNotFoundException.class, () -> bulkOperationService.executeDryRunOperation(id));
    }

    @Test
    void executeDryRun_notCompleted_throws() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .status(BulkOperationStatus.VALIDATED_DRY_RUN)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(exceptionFactory.dryRunOperationNotCompletedException(id))
                .thenReturn(new BulkOperationNotCompletedException("wait"));

        assertThrows(BulkOperationNotCompletedException.class, () -> bulkOperationService.executeDryRunOperation(id));
    }

    @Test
    void executeDryRun_completed_resetsAndPublishesProcessing() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.DRY_RUN_COMPLETED)
                .processedRows(5)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(bulkOperationRepository.save(any(BulkOperation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);

        BulkOperationResponse r = bulkOperationService.executeDryRunOperation(id);

        assertEquals(BulkOperationStatus.VALIDATED, r.getStatus());
        assertNull(op.getProcessedRows());
        verify(messagePublisher).publish(eq(QueueType.BULK_OPERATION_PROCESSING), anyString(), anyMap());
    }

    @Test
    void publishToProcessingQueue_failureIgnored() {
        UUID id = UUID.randomUUID();
        BulkOperation op = BulkOperation.builder()
                .operationIdentifier(id)
                .operationType(BulkOperationType.DROPOFF)
                .status(BulkOperationStatus.DRY_RUN_COMPLETED)
                .build();
        when(bulkOperationRepository.findByOperationIdentifier(id)).thenReturn(Optional.of(op));
        when(bulkOperationRepository.save(any(BulkOperation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messagePublisherFactory.getPublisher()).thenThrow(new RuntimeException("pub fail"));

        assertDoesNotThrow(() -> bulkOperationService.executeDryRunOperation(id));
    }
}
