package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.payload.NotificationPayloadBuilderFactory;
import com.nivasafinance.notification.orchestrator.repository.NotificationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRecordService {

    private final NotificationRecordRepository notificationRecordRepository;
    private final NotificationPayloadBuilderFactory payloadBuilderFactory;
    // Called internally from createNotificationRecordFromEvent, so no separate @Transactional needed
    // The transaction is managed by the calling method
    private Optional<NotificationRecord> createNotificationRecord(Long notificationConfigId,
                                                                  String idempotencyKey,
                                                                  Map<String, Object> payload,
                                                                  Map<String, Object> details) {
        if (idempotencyKey != null) {
            Optional<NotificationRecord> existing = notificationRecordRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Notification record already exists for idempotencyKey={}", idempotencyKey);
                return Optional.empty();
            }
        }

        NotificationRecord record = NotificationRecord.builder()
                .id(UUID.randomUUID())
                .notificationConfigId(notificationConfigId)
                .idempotencyKey(idempotencyKey)
                .notificationPayload(payload)
                .details(details)
                .status(NotificationStatus.INITIATED)
                .build();

        record.setCreatedBy("system");
        record.setUpdatedBy("system");

        NotificationRecord saved = notificationRecordRepository.save(record);
        
        // Force immediate write to database before returning
        // This ensures the record is visible to other transactions (like the queue consumer)
        notificationRecordRepository.flush();
        
        log.debug("Notification record saved and flushed to DB: {}", saved.getId());
        return Optional.of(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<NotificationRecord> createNotificationRecordFromEvent(NotificationEventMapping mapping,
                                                                          String eventCode,
                                                                          Object eventPayload) {
        BusinessEvent businessEvent = resolveBusinessEvent(eventCode);
        Map<String, Object> notificationPayload = payloadBuilderFactory.build(businessEvent, eventPayload);

        String entityId = extractEntityIdFromPayload(notificationPayload);
        if (entityId == null || entityId.isBlank()) {
            log.warn("Skipping notification record creation for event {} because entityId is missing in payload", eventCode);
            return Optional.empty();
        }

        String idempotencyKey = String.format("%s_%s_%s",
                entityId,
                eventCode,
                mapping.getNotificationConfig().getId());

        // Store event_type in details for later retrieval
        Map<String, Object> details = new HashMap<>();
        details.put("event_type", eventCode);

        return createNotificationRecord(
                mapping.getNotificationConfig().getId(),
                idempotencyKey,
                notificationPayload,
                details
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationRecord createManualNotification(Long notificationConfigId,
                                                       String referenceId,
                                                       Map<String, Object> payload) {
        String idempotencyKey = referenceId != null
                ? String.format("%s_MANUAL_%s", referenceId, notificationConfigId)
                : null;
        
        // Store MANUAL as event_type for manual notifications
        Map<String, Object> details = new HashMap<>();
        details.put("event_type", "MANUAL");
        
        Optional<NotificationRecord> created = createNotificationRecord(notificationConfigId, idempotencyKey, payload, details);
        if (created.isPresent()) {
            return created.get();
        }
        if (idempotencyKey != null) {
            return notificationRecordRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Manual notification already exists but cannot be retrieved"));
        }
        throw new IllegalStateException("Manual notification already created");
    }

    @Transactional
    public NotificationRecord updateStatus(UUID recordId, NotificationStatus status) {
        NotificationRecord record = notificationRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Notification record not found: " + recordId));
        record.setStatus(status);
        record.setUpdatedBy("system");
        return notificationRecordRepository.save(record);
    }

    public Optional<NotificationRecord> findById(UUID recordId) {
        return notificationRecordRepository.findById(recordId);
    }

    public Optional<NotificationRecord> findByIdempotencyKey(String idempotencyKey) {
        return notificationRecordRepository.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * Extracts entity ID from notification payload map.
     * Looks for common entity ID fields: entityId, leadId, id (in that order).
     */
    private String extractEntityIdFromPayload(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        Object id = payload.get("entityId");
        if (id == null) {
            id = payload.get("leadId");
        }
        if (id == null) {
            id = payload.get("id");
        }
        return id != null ? id.toString() : null;
    }

    private BusinessEvent resolveBusinessEvent(String eventCode) {
        try {
            return BusinessEvent.valueOf(eventCode);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unsupported business event: " + eventCode, ex);
        }
    }
}


