package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.CallLogResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallReadService {
    CallLogResponse getCallLogByIdentifier(UUID callLogIdentifier);
    List<CallLogResponse> getCallLogsByIDs(List<Long> callLogIDs);
    Optional<CallLogResponse> getCallLogByProviderId(String providerId);
}
