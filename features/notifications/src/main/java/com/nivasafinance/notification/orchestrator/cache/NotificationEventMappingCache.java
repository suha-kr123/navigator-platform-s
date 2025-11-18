package com.nivasafinance.notification.orchestrator.cache;

import com.nivasafinance.notification.orchestrator.entity.NotificationConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import com.nivasafinance.notification.orchestrator.repository.NotificationEventMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventMappingCache {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final NotificationEventMappingRepository eventMappingRepository;

    @Cacheable(cacheNames = "notificationEventMappings", key = "#eventCode")
    public List<NotificationEventMapping> getMappingsForEvent(String eventCode) {
        log.debug("Loading notification mappings for event: {}", eventCode);
        return eventMappingRepository.findByEventAndStatusWithConfig(eventCode, ACTIVE_STATUS);
    }

    public List<NotificationConfig> getConfigsForEvent(String eventCode) {
        return getMappingsForEvent(eventCode).stream()
                .map(NotificationEventMapping::getNotificationConfig)
                .collect(Collectors.toList());
    }

    public boolean hasMapping(String eventCode) {
        return !getMappingsForEvent(eventCode).isEmpty();
    }

    @CacheEvict(cacheNames = "notificationEventMappings", allEntries = true)
    public void clearCache() {
        log.info("Notification event mapping cache cleared");
    }
}


