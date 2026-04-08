package com.nivasafinance.notification.orchestrator.cache;

import com.nivasafinance.notification.orchestrator.entity.NotificationConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import com.nivasafinance.notification.orchestrator.repository.NotificationEventMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventMappingCacheTest {

    @Mock
    private NotificationEventMappingRepository eventMappingRepository;

    private NotificationEventMappingCache cache;

    @BeforeEach
    void setUp() {
        cache = new NotificationEventMappingCache(eventMappingRepository);
    }

    private NotificationEventMapping buildMapping(String event, Long configId, String configName) {
        NotificationConfig config = NotificationConfig.builder()
                .identifier(UUID.randomUUID())
                .name(configName)
                .config(Map.of())
                .status("ACTIVE")
                .build();
        config.setId(configId);
        return NotificationEventMapping.builder()
                .event(event)
                .notificationConfig(config)
                .status("ACTIVE")
                .build();
    }

    @Test
    void getMappingsForEvent_existingEvent_returnsMappings() {
        NotificationEventMapping mapping = buildMapping("LEAD_CREATED", 1L, "Lead Notification");
        when(eventMappingRepository.findByEventAndStatusWithConfig("LEAD_CREATED", "ACTIVE"))
                .thenReturn(List.of(mapping));

        List<NotificationEventMapping> result = cache.getMappingsForEvent("LEAD_CREATED");

        assertEquals(1, result.size(), "Should return 1 mapping");
        assertEquals("LEAD_CREATED", result.get(0).getEvent(), "Event should match");
    }

    @Test
    void getMappingsForEvent_noMappings_returnsEmptyList() {
        when(eventMappingRepository.findByEventAndStatusWithConfig("UNKNOWN_EVENT", "ACTIVE"))
                .thenReturn(Collections.emptyList());

        List<NotificationEventMapping> result = cache.getMappingsForEvent("UNKNOWN_EVENT");

        assertTrue(result.isEmpty(), "Should return empty list for unknown event");
    }

    @Test
    void getMappingsForEvent_multipleMappings_returnsAll() {
        NotificationEventMapping mapping1 = buildMapping("LEAD_CREATED", 1L, "WhatsApp Config");
        NotificationEventMapping mapping2 = buildMapping("LEAD_CREATED", 2L, "FCM Config");
        when(eventMappingRepository.findByEventAndStatusWithConfig("LEAD_CREATED", "ACTIVE"))
                .thenReturn(List.of(mapping1, mapping2));

        List<NotificationEventMapping> result = cache.getMappingsForEvent("LEAD_CREATED");

        assertEquals(2, result.size(), "Should return 2 mappings");
    }

    @Test
    void getConfigsForEvent_returnsMappedConfigs() {
        NotificationEventMapping mapping = buildMapping("LEAD_CREATED", 1L, "Lead Config");
        when(eventMappingRepository.findByEventAndStatusWithConfig("LEAD_CREATED", "ACTIVE"))
                .thenReturn(List.of(mapping));

        List<NotificationConfig> result = cache.getConfigsForEvent("LEAD_CREATED");

        assertEquals(1, result.size(), "Should return 1 config");
        assertEquals("Lead Config", result.get(0).getName(), "Config name should match");
    }

    @Test
    void getConfigsForEvent_noMappings_returnsEmptyList() {
        when(eventMappingRepository.findByEventAndStatusWithConfig("UNKNOWN", "ACTIVE"))
                .thenReturn(Collections.emptyList());

        List<NotificationConfig> result = cache.getConfigsForEvent("UNKNOWN");

        assertTrue(result.isEmpty(), "Should return empty list when no configs found");
    }

    @Test
    void hasMapping_existingEvent_returnsTrue() {
        NotificationEventMapping mapping = buildMapping("LEAD_CREATED", 1L, "Config");
        when(eventMappingRepository.findByEventAndStatusWithConfig("LEAD_CREATED", "ACTIVE"))
                .thenReturn(List.of(mapping));

        assertTrue(cache.hasMapping("LEAD_CREATED"), "Should return true for existing mapping");
    }

    @Test
    void hasMapping_noMappings_returnsFalse() {
        when(eventMappingRepository.findByEventAndStatusWithConfig("UNKNOWN", "ACTIVE"))
                .thenReturn(Collections.emptyList());

        assertFalse(cache.hasMapping("UNKNOWN"), "Should return false for no mappings");
    }

    @Test
    void clearCache_executesWithoutError() {
        assertDoesNotThrow(() -> cache.clearCache(), "Cache clear should not throw");
    }
}
