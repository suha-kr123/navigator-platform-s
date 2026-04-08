package com.nivasafinance.services.whatsapp.service;

import com.nivasafinance.services.whatsapp.dto.TemplateLogRequest;
import com.nivasafinance.services.whatsapp.entity.WhatsAppLog;
import com.nivasafinance.services.whatsapp.repository.WhatsAppLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppLogServiceTest {

    @Mock
    private WhatsAppLogRepository whatsAppLogRepository;

    private WhatsAppLogService service;

    @BeforeEach
    void setUp() {
        service = new WhatsAppLogService(whatsAppLogRepository);
    }

    @Test
    void saveTemplateLog_delegatesToRepository() {
        WhatsAppLog log = new WhatsAppLog();
        log.setMessageId("msg-001");
        when(whatsAppLogRepository.save(log)).thenReturn(log);

        WhatsAppLog result = service.saveTemplateLog(log);

        assertEquals("msg-001", result.getMessageId(), "Message ID should match");
        verify(whatsAppLogRepository).save(log);
    }

    @Test
    void updateTemplateStatus_existingLog_updatesStatusAndDeliveredTime() {
        WhatsAppLog existingLog = new WhatsAppLog();
        existingLog.setMessageId("msg-001");
        existingLog.setStatus("sent");
        when(whatsAppLogRepository.findByMessageId("msg-001")).thenReturn(existingLog);
        when(whatsAppLogRepository.save(any(WhatsAppLog.class))).thenReturn(existingLog);

        WhatsAppLog result = service.updateTemplateStatus("msg-001", "delivered", "2025-01-15T10:30:00");

        assertNotNull(result, "Should return updated log");
        assertEquals("delivered", result.getStatus(), "Status should be updated");
        assertEquals(LocalDateTime.parse("2025-01-15T10:30:00"), result.getDeliveredTime(), "Delivered time should be set");
    }

    @Test
    void updateTemplateStatus_existingLog_nullDeliveredAt_skipsDeliveredTime() {
        WhatsAppLog existingLog = new WhatsAppLog();
        existingLog.setMessageId("msg-001");
        when(whatsAppLogRepository.findByMessageId("msg-001")).thenReturn(existingLog);
        when(whatsAppLogRepository.save(any(WhatsAppLog.class))).thenReturn(existingLog);

        WhatsAppLog result = service.updateTemplateStatus("msg-001", "sent", null);

        assertNotNull(result, "Should return updated log");
        assertNull(result.getDeliveredTime(), "Delivered time should remain null");
    }

    @Test
    void updateTemplateStatus_nonExistingLog_returnsNull() {
        when(whatsAppLogRepository.findByMessageId("unknown")).thenReturn(null);

        WhatsAppLog result = service.updateTemplateStatus("unknown", "delivered", null);

        assertNull(result, "Should return null for non-existing log");
        verify(whatsAppLogRepository, never()).save(any());
    }

    @Test
    void getTemplateLogByMessageId_delegatesToRepository() {
        WhatsAppLog log = new WhatsAppLog();
        log.setMessageId("msg-001");
        when(whatsAppLogRepository.findByMessageId("msg-001")).thenReturn(log);

        WhatsAppLog result = service.getTemplateLogByMessageId("msg-001");

        assertEquals("msg-001", result.getMessageId(), "Message ID should match");
    }

    @Test
    void getTemplateLogsByPhoneNumber_delegatesToRepository() {
        WhatsAppLog log = new WhatsAppLog();
        log.setPhoneNumber("9876543210");
        when(whatsAppLogRepository.findByPhoneNumber("9876543210")).thenReturn(List.of(log));

        List<WhatsAppLog> result = service.getTemplateLogsByPhoneNumber("9876543210");

        assertEquals(1, result.size(), "Should return 1 log");
    }

    @Test
    void getTemplateLogsByStatus_delegatesToRepository() {
        when(whatsAppLogRepository.findByStatus("sent")).thenReturn(Collections.emptyList());

        List<WhatsAppLog> result = service.getTemplateLogsByStatus("sent");

        assertTrue(result.isEmpty(), "Should return empty list");
    }

    @Test
    void createTemplateLog_buildsAndSavesLog() {
        TemplateLogRequest request = TemplateLogRequest.builder()
                .messageId("msg-001")
                .phoneNumber("9876543210")
                .templateName("test_template")
                .broadcastName("broadcast_1")
                .status("sent")
                .build();

        when(whatsAppLogRepository.save(any(WhatsAppLog.class))).thenAnswer(inv -> inv.getArgument(0));

        WhatsAppLog result = service.createTemplateLog(request);

        assertNotNull(result, "Should return created log");
        assertEquals("msg-001", result.getMessageId(), "Message ID should match");
        assertEquals("9876543210", result.getPhoneNumber(), "Phone number should match");
        assertEquals("test_template", result.getTemplateName(), "Template name should match");
        assertEquals("broadcast_1", result.getBroadcastName(), "Broadcast name should match");
        assertEquals("sent", result.getStatus(), "Status should match");
        assertNotNull(result.getSentTime(), "Sent time should be set");
    }

    @Test
    void updateTemplateStatusByPhoneNumber_existingLogs_updatesFirstLog() {
        WhatsAppLog log = new WhatsAppLog();
        log.setPhoneNumber("9876543210");
        log.setStatus("sent");
        when(whatsAppLogRepository.findByPhoneNumber("9876543210")).thenReturn(List.of(log));
        when(whatsAppLogRepository.save(any(WhatsAppLog.class))).thenReturn(log);

        WhatsAppLog result = service.updateTemplateStatusByPhoneNumber("9876543210", "delivered", "2025-01-15T10:30:00");

        assertNotNull(result, "Should return updated log");
        assertEquals("delivered", result.getStatus(), "Status should be updated");
    }

    @Test
    void updateTemplateStatusByPhoneNumber_noLogs_returnsNull() {
        when(whatsAppLogRepository.findByPhoneNumber("0000000000")).thenReturn(Collections.emptyList());

        WhatsAppLog result = service.updateTemplateStatusByPhoneNumber("0000000000", "delivered", null);

        assertNull(result, "Should return null when no logs found");
    }

    @Test
    void updateTemplateStatusByPhoneNumber_nullLogs_returnsNull() {
        when(whatsAppLogRepository.findByPhoneNumber("0000000000")).thenReturn(null);

        WhatsAppLog result = service.updateTemplateStatusByPhoneNumber("0000000000", "delivered", null);

        assertNull(result, "Should return null when repository returns null");
    }
}
