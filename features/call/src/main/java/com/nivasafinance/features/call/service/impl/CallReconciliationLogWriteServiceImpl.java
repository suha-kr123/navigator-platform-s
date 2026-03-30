package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.features.call.dto.ReconciliationCorrectionRecord;
import com.nivasafinance.features.call.entity.ReconciliationLog;
import com.nivasafinance.features.call.repository.ReconciliationLogRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReconciliationLogWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CallReconciliationLogWriteServiceImpl implements CallReconciliationLogWriteService {

    private final ReconciliationLogRepositoryWrapper reconciliationLogRepositoryWrapper;

    @Override
    public void recordCorrection(ReconciliationCorrectionRecord record) {
        LocalDateTime now = LocalDateTime.now();
        ReconciliationLog row = new ReconciliationLog();
        row.setCallLogId(record.callLogId());
        row.setExotelCallSid(record.exotelCallSid());
        row.setChanges(record.changes());
        row.setCorrectionSource(record.correctionSource());
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setVersion(0L);
        reconciliationLogRepositoryWrapper.saveWithException(row);
    }
}
