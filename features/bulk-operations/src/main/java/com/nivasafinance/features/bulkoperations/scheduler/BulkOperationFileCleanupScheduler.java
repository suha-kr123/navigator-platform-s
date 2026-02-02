package com.nivasafinance.features.bulkoperations.scheduler;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
// TODO : This has to be moved to the separate job framework once that is implemented
public class BulkOperationFileCleanupScheduler {

    private static final String NO_FILES_TO_CLEANUP = "No files to cleanup";
    private static final String STARTING_CLEANUP_OF_OLD_BULK_OPERATION_FILES = "Starting cleanup of {} old bulk operation files";
    private static final String FILE_CLEANUP_COMPLETED = "File cleanup completed: {} deleted, {} errors";
    private static final String ERROR_IN_FILE_CLEANUP_SCHEDULER = "Error in file cleanup scheduler: {}";
    private static final String FAILED_TO_DELETE_FILE_FOR_OPERATION = "Failed to delete file for operation {}: {}";

    private final BulkOperationRepository bulkOperationRepository;
    private final BulkOperationFileStorageService fileStorageService;

    @Value("${bulk.operations.temp-file-retention-days:30}")
    private int tempFileRetentionDays;

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldFiles() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(tempFileRetentionDays);
            
            List<BulkOperationStatus> terminalStatuses = List.of(
                BulkOperationStatus.COMPLETED,
                BulkOperationStatus.PARTIALLY_COMPLETED,
                BulkOperationStatus.FAILED,
                BulkOperationStatus.CANCELLED,
                BulkOperationStatus.VALIDATION_FAILED,
                BulkOperationStatus.DRY_RUN_COMPLETED
            );
            
            List<BulkOperation> operationsToCleanup = bulkOperationRepository.findOperationsForFileCleanup(terminalStatuses, cutoffDate);
            
            if (operationsToCleanup.isEmpty()) {
                log.debug(NO_FILES_TO_CLEANUP);
                return;
            }
            
            log.info(STARTING_CLEANUP_OF_OLD_BULK_OPERATION_FILES, operationsToCleanup.size());
            
            int deletedCount = 0;
            int errorCount = 0;
            
            for (BulkOperation operation : operationsToCleanup) {
                try {
                    boolean deleted = false;
                    if (operation.getFileStorageKey() != null && !operation.getFileStorageKey().isBlank()) {
                        fileStorageService.deleteFile(operation.getFileStorageKey());
                        operation.setFileStorageKey(null);
                        deleted = true;
                    }
                    if (operation.getSummaryStorageKey() != null && !operation.getSummaryStorageKey().isBlank()) {
                        fileStorageService.deleteFile(operation.getSummaryStorageKey());
                        operation.setSummaryStorageKey(null);
                        deleted = true;
                    }
                    if (deleted) {
                        bulkOperationRepository.save(operation);
                        deletedCount++;
                    }
                } catch (Exception e) {
                    log.warn(FAILED_TO_DELETE_FILE_FOR_OPERATION, operation.getOperationIdentifier(), e.getMessage());
                    errorCount++;
                }
            }
            log.info(FILE_CLEANUP_COMPLETED, deletedCount, errorCount);
        } catch (Exception e) {
            log.error(ERROR_IN_FILE_CLEANUP_SCHEDULER, e);
        }
    }
}
