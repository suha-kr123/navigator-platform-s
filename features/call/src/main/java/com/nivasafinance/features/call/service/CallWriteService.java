package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;

import java.util.UUID;

public interface CallWriteService {
    InitiateCallResponse call(InitiateCallRequest request);
    void updateCallLogByProviderId(String providerId, UpdateCallLog updateCallLog);
    CreateCallLogResponse createCallLog(CallLog callLog);
    void mapCallLogToLead(Long callLogId, Long leadId, Long contactId);

    void mergeAiAnalysisByIdentifier(UUID callLogIdentifier, CallLog.AiAnalysisDetails patch);
    void mergeAiAnalysisByProviderId(String providerId, CallLog.AiAnalysisDetails patch);
}
