package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;

public interface CallWriteService {
    InitiateCallResponse call(InitiateCallRequest request);
    void updateCallLogByProviderId(String providerId, UpdateCallLog updateCallLog);
}
