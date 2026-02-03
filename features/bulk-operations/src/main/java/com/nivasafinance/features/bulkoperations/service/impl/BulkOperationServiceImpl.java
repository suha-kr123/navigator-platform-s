package com.nivasafinance.features.bulkoperations.service.impl;

import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.utils.DigestUtils;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationResponse;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationNotFoundException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationService;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.common.messaging.enums.QueueType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nivasafinance.features.bulkoperations.storage.StoredFileMultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationServiceImpl implements BulkOperationService {

    private static final String BULK_OPERATION_CREATED = "Bulk operation created: {}";
    private static final String BULK_OPERATION_CANCELLED = "Bulk operation cancelled: {}";
    private static final String BULK_OPERATION_SUMMARY_REPORT_READ = "Summary report read for operation {}";
    private static final String FAILED_TO_READ_UPLOAD_FILE = "Failed to read upload file";
    private static final String FILE = "file";
    private static final String SYSTEM = "system";

    private final BulkOperationRepository bulkOperationRepository;
    private final BulkOperationExceptionFactory exceptionFactory;
    private final BulkOperationFileStorageService fileStorageService;
    private final BulkOperationProcessingService processingService;
    private final EntityManager entityManager;
    private final MessagePublisherFactory messagePublisherFactory;

    @org.springframework.beans.factory.annotation.Value("${bulk.operations.duplicate-upload-window-minutes:30}")
    private int duplicateUploadWindowMinutes;

    @Override
    @Transactional
    public BulkOperationResponse uploadCsv(MultipartFile file, BulkOperationType operationType) {
        return uploadCsvInternal(file, operationType, BulkOperationStatus.UPLOADED);
    }

    @Override
    @Transactional
    public BulkOperationResponse uploadCsvDryRun(MultipartFile file, BulkOperationType operationType) {
        return uploadCsvInternal(file, operationType, BulkOperationStatus.UPLOADED_DRY_RUN);
    }

    private BulkOperationResponse uploadCsvInternal(MultipartFile file, BulkOperationType operationType,
                                                   BulkOperationStatus initialStatus) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            log.error(FAILED_TO_READ_UPLOAD_FILE, e);
            throw exceptionFactory.storageSaveFailedException(null, e);
        }

        String fileHash = DigestUtils.sha256Hex(fileBytes);
        String createdBy = UserContext.getUsername();
        if (ValidationUtils.isNullOrEmpty(createdBy)) {
            createdBy = SYSTEM;
        }

        LocalDateTime duplicateWindowStart = LocalDateTime.now().minusMinutes(duplicateUploadWindowMinutes);
        List<BulkOperationStatus> completedStatuses = List.of(
                BulkOperationStatus.COMPLETED,
                BulkOperationStatus.PARTIALLY_COMPLETED,
                BulkOperationStatus.DRY_RUN_COMPLETED);
        List<BulkOperation> existingWithSameHash = bulkOperationRepository
                .findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(createdBy, fileHash, duplicateWindowStart, completedStatuses);
        if (!ValidationUtils.isNullOrEmpty(existingWithSameHash)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime mostRecentCreatedAt = existingWithSameHash.stream()
                    .map(BulkOperation::getCreatedAt)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(now);
            LocalDateTime retryAfter = mostRecentCreatedAt.plusMinutes(duplicateUploadWindowMinutes);
            long minutesRemaining = ChronoUnit.MINUTES.between(now, retryAfter);
            int displayMinutes = (int) Math.max(1, minutesRemaining);
            throw exceptionFactory.duplicateUploadException(displayMinutes);
        }

        BulkOperation operation = BulkOperation.builder()
                .operationType(operationType)
                .status(initialStatus)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize() > 0 ? file.getSize() : (long) fileBytes.length)
                .fileHash(fileHash)
                .build();
        operation.setCreatedBy(createdBy);
        operation = bulkOperationRepository.save(operation);

        MultipartFile fileToSave = new StoredFileMultipartFile(
                FILE,
                file.getOriginalFilename(),
                file.getContentType(),
                fileBytes.length,
                fileBytes
        );
        String storageKey = fileStorageService.saveFile(fileToSave, operation.getOperationIdentifier());
        operation.setFileStorageKey(storageKey);
        operation = bulkOperationRepository.save(operation);

        publishToValidationQueue(operation.getOperationIdentifier());

        log.info(BULK_OPERATION_CREATED, operation.getOperationIdentifier());
        return BulkOperationResponse.from(operation);
    }

    @Override
    public BulkOperationResponse getOperationStatus(UUID operationId) {
        BulkOperation operation = bulkOperationRepository.findByOperationIdentifier(operationId)
                .orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
        return BulkOperationResponse.from(operation);
    }

    @Override
    @Transactional
    public void cancelOperation(UUID operationId) {
        BulkOperation operation = bulkOperationRepository.findByOperationIdentifier(operationId)
                .orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
        if (!operation.getStatus().canCancel()) {
            throw exceptionFactory.bulkOperationNotCancellableException(operationId);
        }
        operation.setStatus(BulkOperationStatus.CANCELLED);
        operation.setCancelledAt(LocalDateTime.now());
        bulkOperationRepository.save(operation);
        log.info(BULK_OPERATION_CANCELLED, operationId);
    }

    @Override
    public String getSummaryReportCsv(UUID operationId) {
        BulkOperation operation = bulkOperationRepository.findByOperationIdentifier(operationId)
                .orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(operationId));
        if (ValidationUtils.isNullOrEmpty(operation.getSummaryStorageKey())) {
            throw exceptionFactory.bulkOperationReportNotAvailableException(operationId);
        }
        try (InputStream in = fileStorageService.fetchFile(operation.getSummaryStorageKey())) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(BULK_OPERATION_SUMMARY_REPORT_READ, operationId, e);
            throw new BulkOperationNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public PaginatedResponse<BulkOperationResponse> getOperationHistory(PaginationRequest paginationRequest) {
        String username = UserContext.getUsername();
        if (ValidationUtils.isNullOrEmpty(username)) {
            return new PaginatedResponse<>(List.of(), new PaginationInfo(0, 0, 0, 0, 0, false, false));
        }
        int offset = Math.max(0, paginationRequest.getOffset());
        int limit = Math.max(1, paginationRequest.getLimit());
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<BulkOperation> page = bulkOperationRepository.findByCreatedByOrderByCreatedAtDesc(username, pageable);

        List<BulkOperationResponse> content = page.getContent().stream()
                .map(BulkOperationResponse::from)
                .collect(Collectors.toList());
        PaginationInfo paginationInfo = new PaginationInfo(
                offset,
                limit,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious());
        return new PaginatedResponse<>(content, paginationInfo);
    }

    @Override
    @Transactional
    public BulkOperationResponse executeDryRunOperation(UUID dryRunOperationId) {
        BulkOperation operation = bulkOperationRepository.findByOperationIdentifier(dryRunOperationId)
                .orElseThrow(() -> exceptionFactory.bulkOperationNotFoundException(dryRunOperationId));
        if (!operation.getStatus().isDryRunFlow()) {
            throw exceptionFactory.bulkOperationNotFoundException(dryRunOperationId);
        }

        // Only DRY_RUN_COMPLETED: user clicked "Execute for real" – run actual processing
        if (operation.getStatus() != BulkOperationStatus.DRY_RUN_COMPLETED) {
            throw exceptionFactory.dryRunOperationNotCompletedException(dryRunOperationId);
        }
        operation.setStatus(BulkOperationStatus.VALIDATED);
        operation.setProcessedRows(null);
        operation.setSuccessfulRows(null);
        operation.setFailedRows(null);
        operation.setCurrentBatch(null);
        operation.setTotalBatches(null);
        operation.setProcessingStartedAt(null);
        operation.setProcessingCompletedAt(null);
        operation.setSummaryStorageKey(null);
        operation.setTimeoutAt(LocalDateTime.now().plus(24, ChronoUnit.HOURS));
        operation = bulkOperationRepository.save(operation);
        publishToProcessingQueue(operation.getOperationIdentifier());
        return BulkOperationResponse.from(operation);
    }

    /**
     * Publishes to BULK_OPERATION_VALIDATION queue so the listener can pick it up immediately.
     * If publish fails, DB polling will still pick up the UPLOADED operation.
     */
    private void publishToValidationQueue(UUID operationId) {
        try {
            messagePublisherFactory.getPublisher().publish(
                    QueueType.BULK_OPERATION_VALIDATION,
                    operationId.toString(),
                    Map.of("operationId", operationId.toString()));
            log.debug("Published bulk operation {} to validation queue", operationId);
        } catch (Exception e) {
            log.warn("Failed to publish bulk operation {} to validation queue: {}. Will be picked up by DB polling.",
                    operationId, e.getMessage());
        }
    }

    /**
     * Publishes to BULK_OPERATION_PROCESSING queue so the listener can pick it up for real execution.
     * If publish fails, DB polling will still pick up VALIDATED operations.
     */
    private void publishToProcessingQueue(UUID operationId) {
        try {
            messagePublisherFactory.getPublisher().publish(
                    QueueType.BULK_OPERATION_PROCESSING,
                    operationId.toString(),
                    Map.of("operationId", operationId.toString()));
            log.debug("Published bulk operation {} to processing queue for real execution", operationId);
        } catch (Exception e) {
            log.warn("Failed to publish bulk operation {} to processing queue: {}. Will be picked up by DB polling.",
                    operationId, e.getMessage());
        }
    }
}