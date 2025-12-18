package com.nivasafinance.notification.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
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
    private final ObjectMapper objectMapper;
    // Called internally from createNotificationRecordFromEvent, so no separate @Transactional needed
    // The transaction is managed by the calling method
    private Optional<NotificationRecord> createNotificationRecord(Long notificationConfigId,
                                                                  Map<String, Object> payload,
                                                                  Map<String, Object> details) {
        NotificationRecord record = NotificationRecord.builder()
                .id(UUID.randomUUID())
                .notificationConfigId(notificationConfigId)
                .idempotencyKey(null)
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
        // Convert eventPayload to Map for storage (store as-is)
        Map<String, Object> notificationPayload = convertToMap(eventPayload);

        // Store event_type in details for later retrieval
        Map<String, Object> details = new HashMap<>();
        details.put("event_type", eventCode);

        return createNotificationRecord(
                mapping.getNotificationConfig().getId(),
                notificationPayload,
                details
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationRecord createManualNotification(Long notificationConfigId,
                                                       String referenceId,
                                                       Map<String, Object> payload) {
        // Store MANUAL as event_type for manual notifications
        Map<String, Object> details = new HashMap<>();
        details.put("event_type", "MANUAL");
        
        Optional<NotificationRecord> created = createNotificationRecord(notificationConfigId, payload, details);
        return created.orElseThrow(() -> new IllegalStateException("Failed to create manual notification"));
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
     * Converts the eventPayload object to a Map for storage.
     * Stores the payload as-is without transformation.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object eventPayload) {
        if (eventPayload == null) {
            return new HashMap<>();
        }
        
        // If already a Map, return it
        if (eventPayload instanceof Map) {
            Map<String, Object> result = new HashMap<>();
            ((Map<?, ?>) eventPayload).forEach((key, value) -> 
                result.put(key != null ? key.toString() : "null", value)
            );
            return result;
        }
        
        // Convert POJO to Map using Jackson
        try {
            return objectMapper.convertValue(eventPayload, Map.class);
        } catch (Exception ex) {
            log.warn("Failed to convert eventPayload to Map, storing as string representation", ex);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("_raw", eventPayload.toString());
            fallback.put("_type", eventPayload.getClass().getName());
            return fallback;
        }
    }
}


