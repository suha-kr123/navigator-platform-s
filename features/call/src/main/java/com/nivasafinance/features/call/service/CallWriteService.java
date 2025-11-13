package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.InitiateCallRequest;
import com.nivasafinance.features.call.dto.InitiateCallResponse;

public interface CallWriteService {
    InitiateCallResponse call(InitiateCallRequest request);
}
