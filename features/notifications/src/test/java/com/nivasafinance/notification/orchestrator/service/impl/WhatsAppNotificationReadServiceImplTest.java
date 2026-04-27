package com.nivasafinance.notification.orchestrator.service.impl;

import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepository;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepository;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppNotificationReadServiceImplTest {

    @Mock
    private LeadWhatsAppNotificationRepository leadWhatsAppNotificationRepository;

    @Mock
    private AdvisorWhatsAppNotificationRepository advisorWhatsAppNotificationRepository;

    @Mock
    private NotificationReceiptRepository notificationReceiptRepository;

    @InjectMocks
    private WhatsAppNotificationReadServiceImpl whatsAppNotificationReadService;

    private UUID leadIdentifier;
    private UUID receiptId;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        receiptId = UUID.randomUUID();
    }

    @Test
    void getLeadWhatsAppNotificationLogsByIds_emptyIds_returnsEmptyList() {
        List<WhatsAppNotificationLogResponse> result =
                whatsAppNotificationReadService.getLeadWhatsAppNotificationLogsByIds(List.of());

        assertTrue(result.isEmpty(), "Empty id list should yield no DTOs");
    }

    @Test
    void getLeadWhatsAppNotificationLogsByIds_mapsRowsAndReceipts() {
        LeadWhatsAppNotification row = LeadWhatsAppNotification.builder()
                .id(100L)
                .leadIdentifier(leadIdentifier)
                .receiptId(receiptId)
                .templateName("tpl_a")
                .waId("919876543210")
                .build();
        row.setCreatedAt(LocalDateTime.of(2026, 4, 27, 10, 0));
        row.setUpdatedAt(LocalDateTime.of(2026, 4, 27, 11, 0));
        row.setStatusTrack(Map.of("read_time", "2026-04-27T10:30:00"));

        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId)
                .recipientContact("+919876543210")
                .mode("GALLABOX")
                .build();

        when(leadWhatsAppNotificationRepository.findAllById(List.of(100L))).thenReturn(List.of(row));
        when(notificationReceiptRepository.findAllById(List.of(receiptId))).thenReturn(List.of(receipt));

        List<WhatsAppNotificationLogResponse> result =
                whatsAppNotificationReadService.getLeadWhatsAppNotificationLogsByIds(List.of(100L));

        assertEquals(1, result.size(), "One row should produce one DTO");
        WhatsAppNotificationLogResponse dto = result.get(0);
        assertEquals(100L, dto.getId(), "DTO id should match entity id");
        assertEquals("tpl_a", dto.getTemplateName(), "Template name should be copied");
        assertEquals("GALLABOX", dto.getProvider(), "Provider should come from receipt mode");
        assertNotNull(dto.getCreatedAt(), "Audit createdAt should be present");
        assertNotNull(dto.getStatusTrack(), "Status track map should be present");
        verify(notificationReceiptRepository).findAllById(anyList());
    }

    @Test
    void getAdvisorWhatsAppNotificationLogsByIds_mapsRowsAndReceipts() {
        UUID advisorId = UUID.randomUUID();
        AdvisorWhatsAppNotification row = AdvisorWhatsAppNotification.builder()
                .id(200L)
                .advisorIdentifier(advisorId)
                .receiptId(receiptId)
                .templateName("tpl_b")
                .waId("911112223334")
                .build();
        row.setCreatedAt(LocalDateTime.now());

        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId)
                .mode("GALLABOX")
                .recipientContact("911112223334")
                .build();

        when(advisorWhatsAppNotificationRepository.findAllById(List.of(200L))).thenReturn(List.of(row));
        when(notificationReceiptRepository.findAllById(List.of(receiptId))).thenReturn(List.of(receipt));

        List<WhatsAppNotificationLogResponse> result =
                whatsAppNotificationReadService.getAdvisorWhatsAppNotificationLogsByIds(List.of(200L));

        assertEquals(1, result.size(), "One advisor row should produce one DTO");
        assertEquals(200L, result.get(0).getId(), "DTO id should match entity id");
        assertEquals("tpl_b", result.get(0).getTemplateName(), "Template name should be copied");
    }
}
