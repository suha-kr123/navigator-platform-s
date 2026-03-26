package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.executor.NotificationExecutor;
import com.nivasafinance.notification.executor.factory.NotificationExecutorFactory;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationReceiptService {

    private final NotificationReceiptRepository notificationReceiptRepository;
    private final NotificationExecutorFactory notificationExecutorFactory;
    private final PlatformTransactionManager transactionManager;

    @Transactional
    public NotificationReceipt save(NotificationReceipt receipt) {
        log.info("Persisting notification receipt {}", receipt.getId());
        return notificationReceiptRepository.save(receipt);
    }

    public Optional<NotificationReceipt> findById(UUID receiptId) {
        return notificationReceiptRepository.findById(receiptId);
    }

    /**
     * Executes a notification receipt (actually sends the notification).
     * Used for manual testing or retry scenarios.
     */
    @Transactional
    public void executeReceipt(UUID receiptId) {
        NotificationReceipt receipt = notificationReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Notification receipt not found: " + receiptId));

        if (receipt.getStatus() == NotificationStatus.COMPLETED) {
            log.info("Receipt {} already completed, skipping execution", receiptId);
            return;
        }

        try {
            log.info("Executing receipt {}. Mode: {}, Channel: {}, Recipient: {}",
                    receiptId, receipt.getMode(), receipt.getChannelType(), receipt.getRecipientContact());

            NotificationExecutor executor = notificationExecutorFactory.getExecutor(
                    receipt.getMode(),
                    receipt.getChannelType()
            );

            log.info("About to send notification for receipt {}", receiptId);
            executor.send(receipt, null);
            log.info("Notification sent successfully for receipt {}. Now updating status to COMPLETED...", receiptId);

            receipt.setStatus(NotificationStatus.COMPLETED);
            receipt.setUpdatedBy("system");

            Map<String, Object> remarksMap = new HashMap<>();
            remarksMap.put("status", "COMPLETED");
            remarksMap.put("message", "Notification sent successfully");
            remarksMap.put("timestamp", System.currentTimeMillis());
            receipt.setRemarks(remarksMap);

            NotificationReceipt saved = notificationReceiptRepository.save(receipt);
            notificationReceiptRepository.flush();

            log.info("Receipt {} status updated to COMPLETED and flushed to database. Saved receipt status: {}",
                    receiptId, saved.getStatus());

            log.info("Receipt {} executed successfully", receiptId);
        } catch (Exception ex) {
            log.error("Failed to execute receipt {}", receiptId, ex);
            Map<String, Object> errorJson = buildErrorJson(receipt, ex);
            markReceiptFailedInNewTransaction(receiptId, errorJson);
            throw new IllegalStateException("Failed to execute receipt: " + receiptId, ex);
        }
    }

    /**
     * Marks a receipt as FAILED using a programmatic REQUIRES_NEW transaction.
     * This ensures the error is persisted even when the outer transaction rolls back.
     *
     * Public so callers like NotificationExecutorListener can reuse this
     * instead of duplicating the same logic.
     */
    public void markReceiptFailedInNewTransaction(UUID receiptId, Map<String, Object> errorJson) {
        try {
            TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
            requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            requiresNew.execute(status -> {
                Optional<NotificationReceipt> receiptOpt = notificationReceiptRepository.findById(receiptId);
                if (receiptOpt.isEmpty()) {
                    log.warn("Cannot mark receipt {} as FAILED - receipt not found", receiptId);
                    return null;
                }
                NotificationReceipt r = receiptOpt.get();
                r.setStatus(NotificationStatus.FAILED);
                r.setErrorJson(errorJson);
                r.setUpdatedBy("system");
                Map<String, Object> remarks = new HashMap<>();
                remarks.put("error", errorJson.get("message") != null ? errorJson.get("message") : "Unknown error");
                remarks.put("timestamp", System.currentTimeMillis());
                r.setRemarks(remarks);
                notificationReceiptRepository.save(r);
                notificationReceiptRepository.flush();
                log.info("Receipt {} marked as FAILED in new transaction. Error: {}", receiptId, errorJson.get("message"));
                return null;
            });
        } catch (Exception updateEx) {
            log.error("Failed to mark receipt {} as FAILED; receipt may stay INITIATED.", receiptId, updateEx);
        }
    }

    private Map<String, Object> buildErrorJson(NotificationReceipt receipt, Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        String code = inferErrorCode(message);
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("exceptionType", ex.getClass().getSimpleName());
        if (code != null) {
            map.put("code", code);
        }
        if (ex.getCause() != null) {
            map.put("rootCause", ex.getCause().getMessage());
        }
        if (receipt.getTemplateIdentifier() != null) {
            map.put("templateIdentifier", receipt.getTemplateIdentifier());
        }
        return map;
    }

    private static String inferErrorCode(String message) {
        if (message != null) {
            if (message.contains("NotificationTemplate not found") || message.contains("template exists in n_notification_template")) {
                return "TEMPLATE_NOT_FOUND";
            }
            if (message.contains("Template variable") && message.contains("not found")) {
                return "TEMPLATE_VARIABLE_NOT_FOUND";
            }
        }
        return "EXECUTION_FAILED";
    }
}
