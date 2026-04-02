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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
        try {
            log.debug("Creating notification record for event {} mapping {} with payload type: {}", 
                    eventCode, mapping.getId(), eventPayload != null ? eventPayload.getClass().getName() : "null");
            
            // Convert eventPayload to Map for storage (store as-is)
            Map<String, Object> notificationPayload = convertToMap(eventPayload);
            log.info("Converted payload to map with {} keys: {} for event {} mapping {}", 
                    notificationPayload.size(), notificationPayload.keySet(), eventCode, mapping.getId());

            // Store event_type in details for later retrieval
            Map<String, Object> details = new HashMap<>();
            details.put("event_type", eventCode);

            Optional<NotificationRecord> result = createNotificationRecord(
                    mapping.getNotificationConfig().getId(),
                    notificationPayload,
                    details
            );
            
            if (result.isPresent()) {
                log.info("Successfully created notification record {} for event {} mapping {}", 
                        result.get().getId(), eventCode, mapping.getId());
            } else {
                log.warn("createNotificationRecord returned empty Optional for event {} mapping {}", 
                        eventCode, mapping.getId());
            }
            
            return result;
        } catch (Exception ex) {
            log.error("Exception while creating notification record for event {} mapping {}", 
                    eventCode, mapping.getId(), ex);
            return Optional.empty();
        }
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

    @Transactional
    public boolean updateStatusIfExists(UUID recordId, NotificationStatus status) {
        return updateStatusAndErrorIfExists(recordId, status, null);
    }

    @Transactional
    public boolean updateStatusAndErrorIfExists(UUID recordId, NotificationStatus status, Map<String, Object> errorJson) {
        Optional<NotificationRecord> recordOpt = notificationRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            log.warn("Cannot update status for notification record {} - record not found", recordId);
            return false;
        }
        NotificationRecord record = recordOpt.get();
        record.setStatus(status);
        record.setErrorJson(errorJson);
        record.setUpdatedBy("system");
        notificationRecordRepository.save(record);
        notificationRecordRepository.flush();
        log.info("Updated notification record {} status to {} and flushed to database", recordId, status);
        return true;
    }

    public Optional<NotificationRecord> findById(UUID recordId) {
        return notificationRecordRepository.findById(recordId);
    }

    public Optional<NotificationRecord> findByIdempotencyKey(String idempotencyKey) {
        return notificationRecordRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Transactional
    public int markStaleRecordsAsFailed(long staleThresholdMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(staleThresholdMinutes);
        List<NotificationRecord> staleRecords = notificationRecordRepository.findStaleByStatusesAndCreatedBefore(
                List.of(NotificationStatus.INITIATED, NotificationStatus.PROCESSING),
                cutoff
        );

        if (staleRecords.isEmpty()) {
            return 0;
        }

        log.warn("Found {} stale notification records (older than {} minutes)", staleRecords.size(), staleThresholdMinutes);

        int markedCount = 0;
        for (NotificationRecord record : staleRecords) {
            try {
                NotificationStatus originalStatus = record.getStatus();
                Map<String, Object> errorJson = new HashMap<>();
                errorJson.put("message", "Marked FAILED by sweeper — stuck in " + originalStatus + " since " + record.getCreatedAt());
                errorJson.put("code", "STALE_RECORD_SWEEP");
                record.setStatus(NotificationStatus.FAILED);
                record.setErrorJson(errorJson);
                record.setUpdatedBy("system-sweeper");
                notificationRecordRepository.save(record);
                markedCount++;
                log.info("Sweeper marked stale record {} as FAILED (was {} since {})", record.getId(), originalStatus, record.getCreatedAt());
            } catch (Exception ex) {
                log.error("Sweeper failed to mark record {} as FAILED", record.getId(), ex);
            }
        }

        notificationRecordRepository.flush();
        return markedCount;
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


