package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.ReconciliationCorrectionRecord;

public interface CallReconciliationLogWriteService {

    void recordCorrection(ReconciliationCorrectionRecord record);
}
