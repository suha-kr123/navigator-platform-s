package com.nivasafinance.features.bulkoperations.scheduler;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
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
public class BulkOperationTimeoutScheduler {

    private final BulkOperationRepository bulkOperationRepository;

    @Value("${bulk.operations.validation-timeout-hours:2}")
    private int validationTimeoutHours;

    @Value("${bulk.operations.processing-timeout-hours:24}")
    private int processingTimeoutHours;

    private static final String FOUND_TIMED_OUT_BULK_OPERATIONS_TO_CANCEL = "Found {} timed-out bulk operations to cancel";
    private static final String CANCELLED_TIMED_OUT_BULK_OPERATION = "Cancelled timed-out bulk operation {} (status: {}, timeout: {})";
    private static final String FAILED_TO_CANCEL_TIMED_OUT_OPERATION = "Failed to cancel timed-out operation {}";
    private static final String ERROR_IN_TIMEOUT_SCHEDULER = "Error in timeout scheduler: {}";
    private static final String OPERATION_TIMED_OUT_AND_AUTOMATICALLY_CANCELLED = "Operation timed out and was automatically cancelled";

    @Scheduled(fixedDelayString = "300000") // 5 minutes
    @Transactional
    public void cancelTimedOutOperations() {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<BulkOperationStatus> inProgressStatuses = List.of(
                BulkOperationStatus.VALIDATION_IN_PROGRESS,
                BulkOperationStatus.PROCESSING_IN_PROGRESS
            );

            List<BulkOperation> timedOutOperations = bulkOperationRepository.findTimedOutOperations(inProgressStatuses, now);
            
            if (timedOutOperations.isEmpty()) {
                return;
            }
            
            log.warn(FOUND_TIMED_OUT_BULK_OPERATIONS_TO_CANCEL, timedOutOperations.size());
            
            for (BulkOperation operation : timedOutOperations) {
                try {
                    operation.setStatus(BulkOperationStatus.CANCELLED);
                    operation.setCancelledAt(LocalDateTime.now());
                    operation.setErrorMessage(OPERATION_TIMED_OUT_AND_AUTOMATICALLY_CANCELLED);
                    bulkOperationRepository.save(operation);
                
                    log.warn(CANCELLED_TIMED_OUT_BULK_OPERATION,
                        operation.getOperationIdentifier(),
                        operation.getStatus(),
                        operation.getTimeoutAt()
                    );
                } catch (Exception e) {
                    log.error(FAILED_TO_CANCEL_TIMED_OUT_OPERATION, operation.getOperationIdentifier(), e);
                }
            }
        } catch (Exception e) {
            log.error(ERROR_IN_TIMEOUT_SCHEDULER, e);
        }
    }
}
