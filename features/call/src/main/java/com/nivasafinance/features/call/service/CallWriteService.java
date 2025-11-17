package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;

public interface CallWriteService {
    InitiateCallResponse call(InitiateCallRequest request);
    void updateCallLogByProviderId(String providerId, UpdateCallLog updateCallLog);
    CreateCallLogResponse createCallLog(CallLog callLog);
}
