package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.features.call.dto.ReconciliationCorrectionRecord;
import com.nivasafinance.features.call.entity.ReconciliationLog;
import com.nivasafinance.features.call.repository.ReconciliationLogRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CallReconciliationLogWriteServiceImplTest {

    @Mock
    private ReconciliationLogRepositoryWrapper reconciliationLogRepositoryWrapper;

    @InjectMocks
    private CallReconciliationLogWriteServiceImpl service;

    @Test
    void recordCorrection_persistsReconciliationLog() {
        ReconciliationCorrectionRecord record = new ReconciliationCorrectionRecord(
                11L,
                "sid-1",
                Map.of("status", "UPDATED"),
                "SYSTEM");

        service.recordCorrection(record);

        ArgumentCaptor<ReconciliationLog> captor = ArgumentCaptor.forClass(ReconciliationLog.class);
        verify(reconciliationLogRepositoryWrapper).saveWithException(captor.capture());
        ReconciliationLog saved = captor.getValue();
        assertEquals(11L, saved.getCallLogId());
        assertEquals("sid-1", saved.getExotelCallSid());
        assertEquals("SYSTEM", saved.getCorrectionSource());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(0L, saved.getVersion());
    }
}
