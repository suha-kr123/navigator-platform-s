package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service to process scheduled notifications.
 * This service is called by AWS EventBridge Scheduler via API every 15 minutes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduledService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String TIMEZONE = "Asia/Kolkata";
    private static final ZoneId ZONE_ID = ZoneId.of(TIMEZONE);

    private final NotificationReceiptRepository notificationReceiptRepository;
    private final MessagePublisherFactory messagePublisherFactory;

    /**
     * Processes scheduled notifications that are ready to be sent.
     * This method:
     * 1. Finds all receipts with schedules that have INITIATED status
     * 2. Checks if current time is within the preferred_call_start_time and preferred_call_end_time range
     * 3. Updates status to INITIATED (if not already) so the executor can pick them up
     * 
     * This is called by AWS EventBridge Scheduler every 15 minutes.
     */
    @Transactional
    public int processScheduledNotifications() {
        log.info("Processing scheduled notifications");

        // Find all receipts with schedules that are in INITIATED status
        // We'll query all receipts and filter by schedules in memory for now
        // In production, you might want to add a custom query using JSONB functions
        List<NotificationReceipt> receiptsWithSchedules = notificationReceiptRepository.findAll().stream()
                .filter(receipt -> receipt.getStatus() == NotificationStatus.INITIATED)
                .filter(receipt -> receipt.getSchedules() != null && !receipt.getSchedules().isEmpty())
                .toList();

        int processedCount = 0;
        LocalTime currentTime = LocalTime.now(ZONE_ID);

        for (NotificationReceipt receipt : receiptsWithSchedules) {
            if (isReadyToSend(receipt, currentTime)) {
                // Receipt is ready - check if it's already been published to executor queue
                // We check remarks to see if it's been published (to avoid duplicate publishes)
                Map<String, Object> remarks = receipt.getRemarks();
                boolean alreadyPublished = remarks != null && 
                        "PUBLISHED".equals(remarks.get("executorQueueStatus"));
                
                if (!alreadyPublished) {
                    // Publish to executor queue for execution
                    try {
                        messagePublisherFactory.getPublisher().publish(
                                QueueType.NOTIFICATION_EXECUTOR,
                                receipt.getId().toString(),
                                Map.of("receiptId", receipt.getId().toString())
                        );
                        
                        // Update remarks to mark as published
                        Map<String, Object> updatedRemarks = new java.util.HashMap<>(remarks != null ? remarks : Map.of());
                        updatedRemarks.put("executorQueueStatus", "PUBLISHED");
                        updatedRemarks.put("publishedAt", System.currentTimeMillis());
                        receipt.setRemarks(updatedRemarks);
                        notificationReceiptRepository.save(receipt);
                        
                        log.info("Published receipt {} to executor queue (within preferred time range: {})", 
                                receipt.getId(), currentTime);
                processedCount++;
                    } catch (Exception ex) {
                        log.error("Failed to publish receipt {} to executor queue", receipt.getId(), ex);
                    }
                } else {
                    log.debug("Receipt {} already published to executor queue, skipping", receipt.getId());
                }
            }
        }

        log.info("Processed {} scheduled notifications ready to be sent", processedCount);
        return processedCount;
    }

    /**
     * Checks if a receipt is ready to be sent based on its schedules.
     * A receipt is ready if:
     * - No schedules exist (send immediately)
     * - Current time is within the preferred_call_start_time and preferred_call_end_time range
     */
    private boolean isReadyToSend(NotificationReceipt receipt, LocalTime currentTime) {
        List<Map<String, Object>> schedules = receipt.getSchedules();
        
        if (schedules == null || schedules.isEmpty()) {
            return true; // No schedule, send immediately
        }

        // Check each schedule entry
        for (Map<String, Object> schedule : schedules) {
            String startTimeStr = (String) schedule.get("preferred_call_start_time");
            String endTimeStr = (String) schedule.get("preferred_call_end_time");

            if (startTimeStr != null && endTimeStr != null) {
                try {
                    LocalTime startTime = parseTime(startTimeStr);
                    LocalTime endTime = parseTime(endTimeStr);

                    // Check if current time is within the range
                    if (isTimeInRange(currentTime, startTime, endTime)) {
                        return true;
                    }
                } catch (Exception ex) {
                    log.warn("Failed to parse schedule times for receipt {}: start={}, end={}", 
                            receipt.getId(), startTimeStr, endTimeStr, ex);
                }
            }
        }

        return false; // Not within any schedule range
    }

    /**
     * Checks if a time is within a range (inclusive of start, exclusive of end).
     * Handles cases where end time might be before start time (overnight range).
     */
    private boolean isTimeInRange(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            // Normal range: 09:00 to 18:00
            return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime);
        } else {
            // Overnight range: 22:00 to 06:00
            return !currentTime.isBefore(startTime) || currentTime.isBefore(endTime);
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            throw new IllegalArgumentException("Time string cannot be null or blank");
        }

        // Try HH:mm:ss format first
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            // Try HH:mm format
            try {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e2) {
                throw new IllegalArgumentException("Invalid time format: " + timeStr, e2);
            }
        }
    }
}

