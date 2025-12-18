package com.nivasafinance.notification.orchestrator.listener;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.notification.orchestrator.cache.NotificationEventMappingCache;
import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationEventMappingCache mappingCache;
    private final NotificationRecordService notificationRecordService;
    private final MessagePublisherFactory messagePublisherFactory;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(SystemEvent<?> event) {
        String eventType = event.getEventType();
        log.debug("Received system event: {}", eventType);

        List<NotificationEventMapping> mappings = mappingCache.getMappingsForEvent(eventType);
        if (mappings.isEmpty()) {
            log.debug("No notification mapping configured for event {}", eventType);
            return;
        }

        log.info("Found {} notification mappings for event {}", mappings.size(), eventType);
        for (NotificationEventMapping mapping : mappings) {
            try {
                processMapping(event, eventType, mapping);
            } catch (Exception ex) {
                log.error("Failed to create notification record for event {} mapping {}", eventType, mapping.getId(), ex);
            }
        }
    }

    private void processMapping(SystemEvent<?> event, String eventType, NotificationEventMapping mapping) {
        Optional<NotificationRecord> recordOpt = notificationRecordService.createNotificationRecordFromEvent(
                mapping,
                eventType,
                event.getPayload()
        );

        if (recordOpt.isEmpty()) {
            log.warn("Failed to create notification record for event {} mapping {}. createNotificationRecordFromEvent returned empty Optional.",
                    eventType, mapping.getId());
            return;
        }

        NotificationRecord record = recordOpt.get();
        log.info("Created notification record {} for event {} mapping {}", record.getId(), eventType, mapping.getId());
        publish(record, eventType);
    }

    private void publish(NotificationRecord record, String eventType) {
        try {
            // Check if record is already COMPLETED - if so, skip publishing to avoid duplicate processing
            if (record.getStatus() == NotificationStatus.COMPLETED) {
                log.info("Notification record {} is already COMPLETED, skipping publish to NOTIFICATION queue", record.getId());
                return;
            }
            
            // Only publish recordId - all other details will be fetched from DB
            messagePublisherFactory.getPublisher().publish(
                    QueueType.NOTIFICATION,
                    record.getId().toString(),
                    Map.of("recordId", record.getId().toString())
            );
            log.info("Notification record published to queue: {}", record.getId());
        } catch (Exception ex) {
            log.error("Failed to publish notification record {} to queue", record.getId(), ex);
        }
    }
}


