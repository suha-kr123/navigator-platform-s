package com.nivasafinance.features.advisor.service;

import com.nivasafinance.features.advisor.dto.CreateAdvisorCallRequest;
import com.nivasafinance.features.advisor.dto.CreateAdvisorCallResponse;
import com.nivasafinance.features.advisor.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.advisor.dto.CreateExternalCallLogResponse;
import com.nivasafinance.features.advisor.dto.AdvisorUpdateCallLog;

import java.util.UUID;

public interface AdvisorCallWriteService {

    CreateAdvisorCallResponse callPerson(UUID advisorIdentifier, CreateAdvisorCallRequest request);
    void updateCallLog(UUID advisorIdentifier, String externalId, AdvisorUpdateCallLog request);
    CreateExternalCallLogResponse createExternalCallLog(UUID advisorIdentifier, CreateExternalCallLogRequest request);
}
