package com.nivasafinance.notification.orchestrator.controller;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/manual")
@RequiredArgsConstructor
public class NotificationManualController {

    private final NotificationRecordService notificationRecordService;

    @PostMapping("/trigger")
    public ResponseEntity<NotificationRecord> triggerManualNotification(@RequestBody ManualNotificationRequest request) {
        NotificationRecord record = notificationRecordService.createManualNotification(
                request.getNotificationConfigId(),
                request.getReferenceId(),
                request.getPayload()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @GetMapping("/records/{recordId}")
    public ResponseEntity<NotificationRecord> getNotificationRecord(@PathVariable UUID recordId) {
        return notificationRecordService.findById(recordId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/records/{recordId}/status")
    public ResponseEntity<NotificationRecord> updateStatus(@PathVariable UUID recordId,
                                                           @RequestParam NotificationStatus status) {
        NotificationRecord updated = notificationRecordService.updateStatus(recordId, status);
        return ResponseEntity.ok(updated);
    }

    @Data
    public static class ManualNotificationRequest {
        private Long notificationConfigId;
        private String referenceId;
        private Map<String, Object> payload;
    }
}


