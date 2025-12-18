package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.executor.NotificationExecutor;
import com.nivasafinance.notification.executor.factory.NotificationExecutorFactory;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            // Get the appropriate executor based on mode and channel type
            NotificationExecutor executor = notificationExecutorFactory.getExecutor(
                    receipt.getMode(),
                    receipt.getChannelType()
            );

            // Execute (send the notification)
            // For WhatsApp, the executor uses templates, so no pre-rendered message is needed
            log.info("About to send notification for receipt {}", receiptId);
            executor.send(receipt, null);
            log.info("Notification sent successfully for receipt {}. Now updating status to COMPLETED...", receiptId);

            // Update receipt status to COMPLETED
            receipt.setStatus(NotificationStatus.COMPLETED);
            receipt.setUpdatedBy("system");
            
            // Update remarks to reflect successful execution
            Map<String, Object> remarksMap = new HashMap<>();
            remarksMap.put("status", "COMPLETED");
            remarksMap.put("message", "Notification sent successfully");
            remarksMap.put("timestamp", System.currentTimeMillis());
            receipt.setRemarks(remarksMap);
            
            NotificationReceipt saved = notificationReceiptRepository.save(receipt);
            
            // Force immediate write to database
            notificationReceiptRepository.flush();
            
            log.info("Receipt {} status updated to COMPLETED and flushed to database. Saved receipt status: {}", 
                    receiptId, saved.getStatus());
            
            log.info("Receipt {} executed successfully", receiptId);
        } catch (Exception ex) {
            log.error("Failed to execute receipt {}", receiptId, ex);
            receipt.setStatus(NotificationStatus.FAILED);
            
            // Store error message in remarks (JSONB field)
            Map<String, Object> remarksMap = Map.of(
                    "error", ex.getMessage() != null ? ex.getMessage() : "Unknown error",
                    "timestamp", System.currentTimeMillis()
            );
            receipt.setRemarks(remarksMap);
            receipt.setUpdatedBy("system");
            notificationReceiptRepository.save(receipt);
            
            // Force immediate write to database
            notificationReceiptRepository.flush();
            
            log.info("Receipt {} status updated to FAILED and flushed to database", receiptId);
            throw new IllegalStateException("Failed to execute receipt: " + receiptId, ex);
        }
    }
}

