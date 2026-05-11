package com.nivasafinance.notification.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.orchestrator.entity.NotificationConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.repository.NotificationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRecordServiceTest {

    @Mock
    private NotificationRecordRepository notificationRecordRepository;

    @Mock
    private ObjectMapper objectMapper;

    private NotificationRecordService notificationRecordService;

    @BeforeEach
    void setUp() {
        notificationRecordService = new NotificationRecordService(
                notificationRecordRepository,
                objectMapper);
    }

    private NotificationEventMapping buildMapping(Long configId) {
        NotificationConfig config = NotificationConfig.builder().name("testConfig").build();
        config.setId(configId);
        return NotificationEventMapping.builder()
                .event("LEAD_CREATED")
                .notificationConfig(config)
                .status("ACTIVE")
                .build();
    }

    @Test
    void createNotificationRecordFromEvent_happyPath_returnsRecord() {
        NotificationEventMapping mapping = buildMapping(1L);
        Map<String, Object> payload = new HashMap<>(Map.of("key", "value"));

        UUID recordId = UUID.randomUUID();
        NotificationRecord savedRecord = NotificationRecord.builder()
                .id(recordId).notificationConfigId(1L).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(savedRecord);

        Optional<NotificationRecord> result = notificationRecordService.createNotificationRecordFromEvent(
                mapping, "LEAD_CREATED", payload);

        assertTrue(result.isPresent(), "Should return a record");
        assertEquals(recordId, result.get().getId(), "Record ID should match");
    }

    @Test
    void createNotificationRecordFromEvent_nullPayload_returnsRecordWithEmptyMap() {
        NotificationEventMapping mapping = buildMapping(1L);

        UUID recordId = UUID.randomUUID();
        NotificationRecord savedRecord = NotificationRecord.builder()
                .id(recordId).notificationConfigId(1L).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(savedRecord);

        Optional<NotificationRecord> result = notificationRecordService.createNotificationRecordFromEvent(
                mapping, "LEAD_CREATED", null);

        assertTrue(result.isPresent(), "Should return a record even with null payload");
    }

    @Test
    void createNotificationRecordFromEvent_mapPayload_usesDirectly() {
        NotificationEventMapping mapping = buildMapping(1L);
        Map<String, Object> mapPayload = new HashMap<>();
        mapPayload.put("leadId", "123");

        UUID recordId = UUID.randomUUID();
        NotificationRecord savedRecord = NotificationRecord.builder()
                .id(recordId).notificationConfigId(1L).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(savedRecord);

        Optional<NotificationRecord> result = notificationRecordService.createNotificationRecordFromEvent(
                mapping, "LEAD_CREATED", mapPayload);

        assertTrue(result.isPresent(), "Should return a record for map payload");
        verifyNoInteractions(objectMapper);
    }

    @Test
    void createNotificationRecordFromEvent_exceptionDuringSave_returnsEmpty() {
        NotificationEventMapping mapping = buildMapping(1L);

        when(notificationRecordRepository.save(any(NotificationRecord.class)))
                .thenThrow(new RuntimeException("DB error"));

        Optional<NotificationRecord> result = notificationRecordService.createNotificationRecordFromEvent(
                mapping, "LEAD_CREATED", null);

        assertTrue(result.isEmpty(), "Should return empty on exception");
    }

    @Test
    void createManualNotification_happyPath_returnsRecord() {
        UUID recordId = UUID.randomUUID();
        NotificationRecord savedRecord = NotificationRecord.builder()
                .id(recordId).notificationConfigId(1L).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(savedRecord);

        NotificationRecord result = notificationRecordService.createManualNotification(
                1L, "ref-001", Map.of("key", "value"));

        assertNotNull(result, "Should return a record");
        assertEquals(recordId, result.getId(), "Record ID should match");
    }

    @Test
    void createManualNotification_saveReturnsEmpty_throwsException() {
        when(notificationRecordRepository.save(any(NotificationRecord.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> notificationRecordService.createManualNotification(1L, "ref-001", Map.of()),
                "Should throw when save fails");
    }

    @Test
    void updateStatus_existingRecord_updatesAndReturns() {
        UUID recordId = UUID.randomUUID();
        NotificationRecord record = NotificationRecord.builder()
                .id(recordId).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(record);

        NotificationRecord result = notificationRecordService.updateStatus(recordId, NotificationStatus.COMPLETED);

        assertEquals(NotificationStatus.COMPLETED, result.getStatus(), "Status should be updated");
        assertEquals("system", result.getUpdatedBy(), "UpdatedBy should be 'system'");
    }

    @Test
    void updateStatus_notFound_throwsException() {
        UUID recordId = UUID.randomUUID();
        when(notificationRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> notificationRecordService.updateStatus(recordId, NotificationStatus.COMPLETED),
                "Should throw when record not found");
    }

    @Test
    void updateStatusIfExists_existingRecord_returnsTrue() {
        UUID recordId = UUID.randomUUID();
        NotificationRecord record = NotificationRecord.builder()
                .id(recordId).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(record);

        boolean result = notificationRecordService.updateStatusIfExists(recordId, NotificationStatus.COMPLETED);

        assertTrue(result, "Should return true when record exists");
    }

    @Test
    void updateStatusAndErrorIfExists_notFound_returnsFalse() {
        UUID recordId = UUID.randomUUID();
        when(notificationRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        boolean result = notificationRecordService.updateStatusAndErrorIfExists(
                recordId, NotificationStatus.FAILED, Map.of("error", "test"));

        assertFalse(result, "Should return false when record not found");
    }

    @Test
    void updateStatusAndErrorIfExists_existingRecord_updatesErrorJsonAndStatus() {
        UUID recordId = UUID.randomUUID();
        NotificationRecord record = NotificationRecord.builder()
                .id(recordId).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(record);

        Map<String, Object> errorJson = Map.of("message", "Something failed");
        boolean result = notificationRecordService.updateStatusAndErrorIfExists(
                recordId, NotificationStatus.FAILED, errorJson);

        assertTrue(result, "Should return true");
        assertEquals(NotificationStatus.FAILED, record.getStatus(), "Status should be FAILED");
        assertEquals(errorJson, record.getErrorJson(), "Error JSON should be set");
        verify(notificationRecordRepository).flush();
    }

    @Test
    void findById_delegatesToRepository() {
        UUID recordId = UUID.randomUUID();
        NotificationRecord record = NotificationRecord.builder().id(recordId).build();
        when(notificationRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        Optional<NotificationRecord> result = notificationRecordService.findById(recordId);

        assertTrue(result.isPresent(), "Should find the record");
        assertEquals(recordId, result.get().getId(), "Record ID should match");
    }

    @Test
    void findByIdempotencyKey_delegatesToRepository() {
        NotificationRecord record = NotificationRecord.builder().id(UUID.randomUUID()).idempotencyKey("key-1").build();
        when(notificationRecordRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(record));

        Optional<NotificationRecord> result = notificationRecordService.findByIdempotencyKey("key-1");

        assertTrue(result.isPresent(), "Should find the record by idempotency key");
    }

    @Test
    void markStaleRecordsAsFailed_noStaleRecords_returnsZero() {
        when(notificationRecordRepository.findStaleByStatusesAndCreatedBefore(any(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        int result = notificationRecordService.markStaleRecordsAsFailed(30);

        assertEquals(0, result, "Should return 0 when no stale records");
    }

    @Test
    void markStaleRecordsAsFailed_staleRecords_marksAsFailedAndReturnsCount() {
        NotificationRecord staleRecord = NotificationRecord.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.INITIATED).build();
        when(notificationRecordRepository.findStaleByStatusesAndCreatedBefore(any(), any(LocalDateTime.class)))
                .thenReturn(List.of(staleRecord));
        when(notificationRecordRepository.save(any(NotificationRecord.class))).thenReturn(staleRecord);

        int result = notificationRecordService.markStaleRecordsAsFailed(30);

        assertEquals(1, result, "Should mark 1 stale record");
        assertEquals(NotificationStatus.FAILED, staleRecord.getStatus(), "Status should be FAILED");
        assertEquals("system-sweeper", staleRecord.getUpdatedBy(), "UpdatedBy should be 'system-sweeper'");
        assertNotNull(staleRecord.getErrorJson(), "Error JSON should be set");
        verify(notificationRecordRepository).flush();
    }

    @Test
    void markStaleRecordsAsFailed_exceptionOnOneRecord_continuesProcessing() {
        NotificationRecord record1 = NotificationRecord.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.INITIATED).build();
        NotificationRecord record2 = NotificationRecord.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.PROCESSING).build();
        when(notificationRecordRepository.findStaleByStatusesAndCreatedBefore(any(), any(LocalDateTime.class)))
                .thenReturn(List.of(record1, record2));
        when(notificationRecordRepository.save(record1)).thenThrow(new RuntimeException("DB error"));
        when(notificationRecordRepository.save(record2)).thenReturn(record2);

        int result = notificationRecordService.markStaleRecordsAsFailed(30);

        assertEquals(1, result, "Should count only successfully marked records");
    }
}
