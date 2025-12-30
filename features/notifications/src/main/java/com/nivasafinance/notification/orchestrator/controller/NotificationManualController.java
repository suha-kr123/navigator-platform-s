package com.nivasafinance.notification.orchestrator.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.listener.NotificationReceiptConstructorListener;
import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import com.nivasafinance.notification.orchestrator.service.NotificationReceiptService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/manual")
@RequiredArgsConstructor
@Slf4j
public class NotificationManualController {

    private final NotificationRecordService notificationRecordService;
    private final NotificationReceiptConstructorListener notificationReceiptConstructorListener;
    private final NotificationReceiptService notificationReceiptService;

    @PostMapping("/trigger")
    @RequirePermission(permissionName = "CREATE_NOTIFICATION")
    public ResponseEntity<NotificationRecord> triggerManualNotification(@RequestBody ManualNotificationRequest request) {
        NotificationRecord record = notificationRecordService.createManualNotification(
                request.getNotificationConfigId(),
                request.getReferenceId(),
                request.getPayload()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @GetMapping("/records/{recordId}")
    @RequirePermission(permissionName = "READ_NOTIFICATION")
    public ResponseEntity<NotificationRecord> getNotificationRecord(@PathVariable UUID recordId) {
        return notificationRecordService.findById(recordId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/records/{recordId}/status")
    @RequirePermission(permissionName = "UPDATE_NOTIFICATION")
    public ResponseEntity<NotificationRecord> updateStatus(@PathVariable UUID recordId,
                                                           @RequestParam NotificationStatus status) {
        NotificationRecord updated = notificationRecordService.updateStatus(recordId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Manual endpoint to trigger receipt creation for a notification record.
     * Useful for local testing without SQS.
     * Step 1: Creates receipts for the notification record.
     */
    @PostMapping("/records/{recordId}/process")
    public ResponseEntity<Map<String, Object>> processRecord(@PathVariable UUID recordId) {
        try {
            log.info("Manual processing requested for notification record: {}", recordId);
            
            // Check if record exists
            notificationRecordService.findById(recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Notification record not found: " + recordId));
            
            // Process the record (create receipts)
            boolean success = notificationReceiptConstructorListener.processRecordById(recordId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Notification record processed successfully (receipts created)",
                        "recordId", recordId.toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Failed to process notification record",
                        "recordId", recordId.toString()
                ));
            }
        } catch (Exception ex) {
            log.error("Error processing notification record manually", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to process notification record",
                    "recordId", recordId.toString(),
                    "error", ex.getMessage()
            ));
        }
    }

    /**
     * Manual endpoint to execute/send a notification receipt.
     * Useful for local testing without SQS.
     * Step 2: Actually sends the notification (WhatsApp, SMS, email, etc.).
     */
    @PostMapping("/receipts/{receiptId}/execute")
    public ResponseEntity<Map<String, Object>> executeReceipt(@PathVariable UUID receiptId) {
        try {
            log.info("Manual execution requested for notification receipt: {}", receiptId);
            
            // Check if receipt exists
            NotificationReceipt receipt = notificationReceiptService.findById(receiptId)
                    .orElseThrow(() -> new IllegalArgumentException("Notification receipt not found: " + receiptId));
            
            // Execute the receipt (send notification)
            notificationReceiptService.executeReceipt(receiptId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification receipt executed successfully",
                    "receiptId", receiptId.toString(),
                    "status", receipt.getStatus().toString()
            ));
        } catch (Exception ex) {
            log.error("Error executing notification receipt manually", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to execute notification receipt",
                    "receiptId", receiptId.toString(),
                    "error", ex.getMessage()
            ));
        }
    }

    @Data
    public static class ManualNotificationRequest {
        private Long notificationConfigId;
        private String referenceId;
        private Map<String, Object> payload;
    }
}


