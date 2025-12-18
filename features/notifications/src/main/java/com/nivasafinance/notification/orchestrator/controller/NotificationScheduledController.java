package com.nivasafinance.notification.orchestrator.controller;

import com.nivasafinance.notification.orchestrator.service.NotificationScheduledService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for AWS EventBridge Scheduler to trigger scheduled notification processing.
 * AWS EventBridge Scheduler calls this endpoint every 15 minutes.
 */
@RestController
@RequestMapping("/api/notifications/scheduled")
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduledController {

    private final NotificationScheduledService notificationScheduledService;

    /**
     * Endpoint for AWS EventBridge Scheduler to trigger scheduled notification processing.
     * This endpoint is called every 15 minutes by AWS EventBridge Scheduler.
     * 
     * AWS EventBridge Scheduler configuration:
     * - Schedule expression: rate(15 minutes)
     * - Target: HTTP POST to this endpoint
     * - Payload: Optional (can be empty or contain metadata)
     */
    @PostMapping("/process")
    public ResponseEntity<ScheduledJobResponse> processScheduledNotifications(
            @RequestBody(required = false) ScheduledJobRequest request) {
        log.info("Received scheduled notification processing request from AWS EventBridge Scheduler");
        
        try {
            int processedCount = notificationScheduledService.processScheduledNotifications();
            
            ScheduledJobResponse response = new ScheduledJobResponse();
            response.setSuccess(true);
            response.setProcessedCount(processedCount);
            response.setMessage("Successfully processed " + processedCount + " scheduled notifications");
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to process scheduled notifications", ex);
            
            ScheduledJobResponse response = new ScheduledJobResponse();
            response.setSuccess(false);
            response.setProcessedCount(0);
            response.setMessage("Failed to process scheduled notifications: " + ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Data
    public static class ScheduledJobRequest {
        // Optional: AWS EventBridge can send metadata here
        private String source;
        private String account;
    }

    @Data
    public static class ScheduledJobResponse {
        private boolean success;
        private int processedCount;
        private String message;
    }
}

