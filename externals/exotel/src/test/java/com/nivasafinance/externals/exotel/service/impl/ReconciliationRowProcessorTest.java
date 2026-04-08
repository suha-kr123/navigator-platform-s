package com.nivasafinance.externals.exotel.service.impl;

import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.call.dto.ReconciliationCorrectionRecord;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallReconciliationLogWriteService;
import com.nivasafinance.features.call.service.CallWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationRowProcessorTest {

    @Mock private CallReadService callReadService;
    @Mock private CallWriteService callWriteService;
    @Mock private CallReconciliationLogWriteService callReconciliationLogWriteService;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ReconciliationRowProcessor processor;

    // ── processRow: no change ───────────────────────────────────────
    @Test
    void processRow_whenNoStatusOrDurationChange_returnsNoChange_andNoWrites() {
        CallLog row = new CallLog();
        row.setId(10L);
        row.setProviderId("P1");
        row.setStatus(CallStatus.COMPLETED);
        row.setCompletionDetails(CallLog.CompletionDetails.builder().duration(30L).build());

        ReconciliationRowProcessor.ReconcileResult res =
                processor.processRow(row, CallStatus.COMPLETED, 30L, "JOB");

        assertFalse(res.updated, "No update expected when status and duration unchanged");
        verifyNoInteractions(callWriteService, callReconciliationLogWriteService, applicationEventPublisher);
    }

    // ── processRow: status change only ──────────────────────────────
    @Test
    void processRow_whenStatusDiffers_updatesStatus_recordsCorrection_publishesEvent() {
        CallLog row = new CallLog();
        row.setId(11L);
        row.setIdentifier(java.util.UUID.randomUUID());
        row.setProviderId("P2");
        row.setStatus(CallStatus.IN_PROGRESS);

        when(callReadService.findLeadIdByCallLogId(11L)).thenReturn(Optional.of(99L));

        ReconciliationRowProcessor.ReconcileResult res =
                processor.processRow(row, CallStatus.COMPLETED, null, "JOB");

        assertTrue(res.updated, "Update expected when status differs");
        assertTrue(res.statusChanged, "Status change should be true");
        assertFalse(res.durationChanged, "Duration change should be false");

        ArgumentCaptor<UpdateCallLog> updateCaptor = ArgumentCaptor.forClass(UpdateCallLog.class);
        verify(callWriteService).updateCallLogByProviderId(eq("P2"), updateCaptor.capture());
        assertEquals(CallStatus.COMPLETED, updateCaptor.getValue().getStatus(),
                "New status should be written");

        verify(callReconciliationLogWriteService).recordCorrection(any(ReconciliationCorrectionRecord.class));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    // ── processRow: duration change only ────────────────────────────
    @Test
    void processRow_whenDurationDiffers_updatesDuration_recordsCorrection_publishesEvent() {
        CallLog row = new CallLog();
        row.setId(12L);
        row.setIdentifier(java.util.UUID.randomUUID());
        row.setProviderId("P3");
        row.setStatus(CallStatus.COMPLETED);
        row.setCompletionDetails(CallLog.CompletionDetails.builder().duration(10L).build());

        when(callReadService.findLeadIdByCallLogId(12L)).thenReturn(Optional.of(88L));

        ReconciliationRowProcessor.ReconcileResult res =
                processor.processRow(row, CallStatus.COMPLETED, 25L, "JOB");

        assertTrue(res.updated, "Update expected when duration differs");
        assertFalse(res.statusChanged, "Status should not change");
        assertTrue(res.durationChanged, "Duration should change");

        ArgumentCaptor<UpdateCallLog> updateCaptor = ArgumentCaptor.forClass(UpdateCallLog.class);
        verify(callWriteService).updateCallLogByProviderId(eq("P3"), updateCaptor.capture());
        assertEquals(25L, updateCaptor.getValue().getCompletionDetails().getDuration(),
                "New duration should be written");

        verify(callReconciliationLogWriteService).recordCorrection(any(ReconciliationCorrectionRecord.class));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }
}

