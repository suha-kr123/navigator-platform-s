package com.nivasafinance.features.call.dto;

import java.util.Map;

public record ReconciliationCorrectionRecord(
        Long callLogId,
        String exotelCallSid,
        Map<String, Object> changes,
        String correctionSource) {
}
