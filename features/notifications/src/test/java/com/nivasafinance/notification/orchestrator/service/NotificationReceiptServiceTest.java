package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.executor.NotificationExecutor;
import com.nivasafinance.notification.executor.factory.NotificationExecutorFactory;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationReceiptServiceTest {

    @Mock
    private NotificationReceiptRepository notificationReceiptRepository;

    @Mock
    private NotificationExecutorFactory notificationExecutorFactory;

    @Mock
    private PlatformTransactionManager transactionManager;

    private NotificationReceiptService notificationReceiptService;

    @BeforeEach
    void setUp() {
        notificationReceiptService = new NotificationReceiptService(
                notificationReceiptRepository, notificationExecutorFactory, transactionManager);
    }

    @Test
    void save_delegatesToRepository() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.INITIATED).build();
        when(notificationReceiptRepository.save(receipt)).thenReturn(receipt);

        NotificationReceipt result = notificationReceiptService.save(receipt);

        assertEquals(receipt.getId(), result.getId(), "Saved receipt ID should match");
        verify(notificationReceiptRepository).save(receipt);
    }

    @Test
    void findById_delegatesToRepository() {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder().id(receiptId).build();
        when(notificationReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        Optional<NotificationReceipt> result = notificationReceiptService.findById(receiptId);

        assertTrue(result.isPresent(), "Should find the receipt");
        assertEquals(receiptId, result.get().getId(), "Receipt ID should match");
    }

    @Test
    void executeReceipt_receiptNotFound_throwsException() {
        UUID receiptId = UUID.randomUUID();
        when(notificationReceiptRepository.findById(receiptId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> notificationReceiptService.executeReceipt(receiptId),
                "Should throw when receipt not found");
    }

    @Test
    void executeReceipt_alreadyCompleted_skipsExecution() {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.COMPLETED).build();
        when(notificationReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        notificationReceiptService.executeReceipt(receiptId);

        verifyNoInteractions(notificationExecutorFactory);
        verify(notificationReceiptRepository, never()).save(any());
    }

    @Test
    void executeReceipt_happyPath_sendsAndMarksCompleted() throws Exception {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.INITIATED)
                .mode("WATI").channelType("WHATSAPP").build();
        when(notificationReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        NotificationExecutor executor = mock(NotificationExecutor.class);
        when(notificationExecutorFactory.getExecutor("WATI", "WHATSAPP")).thenReturn(executor);
        when(notificationReceiptRepository.save(any(NotificationReceipt.class))).thenReturn(receipt);

        notificationReceiptService.executeReceipt(receiptId);

        verify(executor).send(receipt, null);
        assertEquals(NotificationStatus.COMPLETED, receipt.getStatus(), "Status should be COMPLETED");
        assertNotNull(receipt.getRemarks(), "Remarks should be set");
        verify(notificationReceiptRepository).flush();
    }

    @Test
    void executeReceipt_executorThrows_marksFailedAndRethrows() throws Exception {
        UUID receiptId = UUID.randomUUID();
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(receiptId).status(NotificationStatus.INITIATED)
                .mode("WATI").channelType("WHATSAPP").build();
        when(notificationReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        NotificationExecutor executor = mock(NotificationExecutor.class);
        when(notificationExecutorFactory.getExecutor("WATI", "WHATSAPP")).thenReturn(executor);
        doThrow(new RuntimeException("Send failed")).when(executor).send(receipt, null);

        assertThrows(IllegalStateException.class,
                () -> notificationReceiptService.executeReceipt(receiptId),
                "Should rethrow as IllegalStateException");
    }

    @Test
    void markStaleReceiptsAsFailed_noStaleReceipts_returnsZero() {
        when(notificationReceiptRepository.findStaleByStatusesAndCreatedBefore(any(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        int result = notificationReceiptService.markStaleReceiptsAsFailed(30);

        assertEquals(0, result, "Should return 0 when no stale receipts");
    }

    @Test
    void markStaleReceiptsAsFailed_staleReceipts_marksFailedAndReturnsCount() {
        NotificationReceipt staleReceipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.INITIATED).build();
        when(notificationReceiptRepository.findStaleByStatusesAndCreatedBefore(any(), any(LocalDateTime.class)))
                .thenReturn(List.of(staleReceipt));
        when(notificationReceiptRepository.save(any(NotificationReceipt.class))).thenReturn(staleReceipt);

        int result = notificationReceiptService.markStaleReceiptsAsFailed(30);

        assertEquals(1, result, "Should mark 1 stale receipt");
        assertEquals(NotificationStatus.FAILED, staleReceipt.getStatus(), "Status should be FAILED");
        assertEquals("system-sweeper", staleReceipt.getUpdatedBy(), "UpdatedBy should be 'system-sweeper'");
        assertNotNull(staleReceipt.getErrorJson(), "Error JSON should be set");
        verify(notificationReceiptRepository).flush();
    }

    @Test
    void markStaleReceiptsAsFailed_exceptionOnOneReceipt_continuesProcessing() {
        NotificationReceipt receipt1 = NotificationReceipt.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.INITIATED).build();
        NotificationReceipt receipt2 = NotificationReceipt.builder()
                .id(UUID.randomUUID()).status(NotificationStatus.PROCESSING).build();
        when(notificationReceiptRepository.findStaleByStatusesAndCreatedBefore(any(), any(LocalDateTime.class)))
                .thenReturn(List.of(receipt1, receipt2));
        when(notificationReceiptRepository.save(receipt1)).thenThrow(new RuntimeException("DB error"));
        when(notificationReceiptRepository.save(receipt2)).thenReturn(receipt2);

        int result = notificationReceiptService.markStaleReceiptsAsFailed(30);

        assertEquals(1, result, "Should count only successfully marked receipts");
    }
}
