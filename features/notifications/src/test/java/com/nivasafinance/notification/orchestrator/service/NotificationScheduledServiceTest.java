package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.common.messaging.factory.MessagePublisherFactory;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationScheduledServiceTest {

    @Mock
    private NotificationReceiptRepository notificationReceiptRepository;

    @Mock
    private MessagePublisherFactory messagePublisherFactory;

    @Mock
    private MessagePublisher messagePublisher;

    private NotificationScheduledService service;

    @BeforeEach
    void setUp() {
        service = new NotificationScheduledService(notificationReceiptRepository, messagePublisherFactory);
    }

    @Test
    void processScheduledNotifications_noReceiptsFound_returnsZero() {
        when(notificationReceiptRepository.findAll()).thenReturn(Collections.emptyList());

        int result = service.processScheduledNotifications();

        assertEquals(0, result, "Should return 0 when no receipts found");
    }

    @Test
    void processScheduledNotifications_noInitiatedReceipts_returnsZero() {
        NotificationReceipt completedReceipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.COMPLETED)
                .schedules(List.of(Map.of("preferred_call_start_time", "00:00", "preferred_call_end_time", "23:59")))
                .build();
        when(notificationReceiptRepository.findAll()).thenReturn(List.of(completedReceipt));

        int result = service.processScheduledNotifications();

        assertEquals(0, result, "Should return 0 for non-INITIATED receipts");
    }

    @Test
    void processScheduledNotifications_noSchedules_treatedAsReady() {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.INITIATED)
                .schedules(null).remarks(null).build();
        when(notificationReceiptRepository.findAll()).thenReturn(List.of(receipt));

        int result = service.processScheduledNotifications();

        assertEquals(0, result, "Receipt without schedules should not be in findAll filtered list");
    }

    @Test
    void processScheduledNotifications_alreadyPublished_skips() {
        UUID receiptId = UUID.randomUUID();
        Map<String, Object> remarks = new HashMap<>();
        remarks.put("executorQueueStatus", "PUBLISHED");
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.INITIATED)
                .schedules(List.of(Map.of("preferred_call_start_time", "00:00", "preferred_call_end_time", "23:59")))
                .remarks(remarks).build();
        when(notificationReceiptRepository.findAll()).thenReturn(List.of(receipt));

        int result = service.processScheduledNotifications();

        assertEquals(0, result, "Should skip already published receipts");
        verifyNoInteractions(messagePublisherFactory);
    }

    @Test
    void processScheduledNotifications_withinTimeRange_publishesAndReturnsCount() {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.INITIATED)
                .schedules(List.of(Map.of("preferred_call_start_time", "00:00", "preferred_call_end_time", "23:59")))
                .remarks(null).build();
        when(notificationReceiptRepository.findAll()).thenReturn(List.of(receipt));
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);
        when(notificationReceiptRepository.save(any(NotificationReceipt.class))).thenReturn(receipt);

        int result = service.processScheduledNotifications();

        assertEquals(1, result, "Should publish 1 receipt within time range");
        verify(messagePublisher).publish(eq(QueueType.NOTIFICATION_EXECUTOR), anyString(), any(Map.class));
        verify(notificationReceiptRepository).save(receipt);
    }

    @Test
    void processScheduledNotifications_publishFails_doesNotCount() {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.INITIATED)
                .schedules(List.of(Map.of("preferred_call_start_time", "00:00", "preferred_call_end_time", "23:59")))
                .remarks(null).build();
        when(notificationReceiptRepository.findAll()).thenReturn(List.of(receipt));
        when(messagePublisherFactory.getPublisher()).thenReturn(messagePublisher);
        doThrow(new RuntimeException("Queue error")).when(messagePublisher)
                .publish(any(QueueType.class), anyString(), any(Map.class));

        int result = service.processScheduledNotifications();

        assertEquals(0, result, "Should not count failed publishes");
    }
}
