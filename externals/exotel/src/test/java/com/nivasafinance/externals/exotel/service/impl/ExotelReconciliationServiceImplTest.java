package com.nivasafinance.externals.exotel.service.impl;

import com.nivasafinance.externals.exotel.dto.ExotelReconciliationSummary;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.VoiceGetCallStatusResponse;
import com.nivasafinance.services.voice.dto.VoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExotelReconciliationServiceImplTest {

    @Mock private CallReadService callReadService;
    @Mock private ReconciliationRowProcessor rowProcessor;
    @Mock private ServiceFactory<VoiceHandler> voiceServiceFactory;

    @InjectMocks
    private ExotelReconciliationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        // make batchSize small to exercise paging quickly
        setPrivateInt("batchSize", 2);
        setPrivateInt("historicalBackfillLookbackDays", 7);
        setPrivateInt("manualMaxRangeDays", 10);
        setPrivateString("reconciliationZone", "");
    }

    private void setPrivateInt(String name, int value) throws Exception {
        Field f = ExotelReconciliationServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(service, value);
    }

    private void setPrivateString(String name, String value) throws Exception {
        Field f = ExotelReconciliationServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    // ── reconcileDateRange: validation ──────────────────────────────
    @Test
    void reconcileDateRange_nullDates_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.reconcileDateRange(null, LocalDate.now()),
                "startDate and endDate are required");
        assertThrows(IllegalArgumentException.class,
                () -> service.reconcileDateRange(LocalDate.now(), null),
                "startDate and endDate are required");
    }

    @Test
    void reconcileDateRange_startAfterEnd_throws() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> service.reconcileDateRange(start, end),
                "startDate must not be after endDate");
    }

    @Test
    void reconcileDateRange_spanExceedsMax_throws() throws Exception {
        setPrivateInt("manualMaxRangeDays", 2);
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();
        assertThrows(IllegalArgumentException.class,
                () -> service.reconcileDateRange(start, end),
                "Date range exceeds configured manual max days");
    }

    // ── runPagedReconciliation happy path with updates ──────────────
    @Test
    void reconcileDateRange_pagesThroughAndAggregatesSummary() {
        // Two pages: first with 2 rows, second with 1, third empty to end
        CallLog r1 = buildRow(1L, "P1", CallStatus.IN_PROGRESS, null);
        CallLog r2 = buildRow(2L, "P2", CallStatus.NO_ANSWER, null);
        CallLog r3 = buildRow(3L, "P3", CallStatus.COMPLETED, null);

        // Mock voice API snapshot fetch to succeed for any provider id
        VoiceHandler vh = mock(VoiceHandler.class);
        when(voiceServiceFactory.getHandler(any())).thenReturn(vh);
        VoiceGetCallStatusResponse snap = mock(VoiceGetCallStatusResponse.class);
        when(snap.getStatus()).thenReturn(VoiceStatus.COMPLETED);
        when(vh.getCallStatus(anyString(), any())).thenReturn(snap);

        when(callReadService.findByProviderAndCreatedAtRange(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1, r2), org.springframework.data.domain.PageRequest.of(0, 2), 3))
                .thenReturn(new PageImpl<>(List.of(r3), org.springframework.data.domain.PageRequest.of(1, 2), 3))
                .thenReturn(new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(2, 2), 3));

        when(rowProcessor.processRow(eq(r1), any(), any(), any()))
                .thenReturn(reconcileResult(true, true, false));
        when(rowProcessor.processRow(eq(r2), any(), any(), any()))
                .thenReturn(reconcileResult(false, false, false));
        when(rowProcessor.processRow(eq(r3), any(), any(), any()))
                .thenReturn(reconcileResult(true, false, true));

        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();
        ExotelReconciliationSummary s = service.reconcileDateRange(start, end);

        assertEquals(3, s.rowsFetched(), "Should count all rows fetched across pages");
        assertEquals(2, s.rowsUpdated(), "Should count rows where update occurred");
        assertEquals(1, s.statusCorrections(), "Should count status changes");
        assertEquals(1, s.durationCorrections(), "Should count duration changes");
        assertEquals(0, s.apiFailures(), "No API failures when processor returns results");
        assertEquals(0, s.skippedInvalidRows(), "No skipped rows here");
    }

    private CallLog buildRow(Long id, String providerId, CallStatus status, Long duration) {
        CallLog row = new CallLog();
        row.setId(id);
        row.setProviderId(providerId);
        row.setStatus(status);
        if (duration != null) {
            row.setCompletionDetails(CallLog.CompletionDetails.builder().duration(duration).build());
        }
        return row;
    }

    private ReconciliationRowProcessor.ReconcileResult reconcileResult(boolean updated, boolean statusChanged, boolean durationChanged) {
        try {
            Class<?> cls = Class.forName("com.nivasafinance.externals.exotel.service.impl.ReconciliationRowProcessor$ReconcileResult");
            var ctor = cls.getDeclaredConstructor(boolean.class, boolean.class, boolean.class);
            ctor.setAccessible(true);
            return (ReconciliationRowProcessor.ReconcileResult) ctor.newInstance(updated, statusChanged, durationChanged);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

