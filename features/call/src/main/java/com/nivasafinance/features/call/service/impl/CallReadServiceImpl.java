package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.repository.CallLogRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CallReadServiceImpl implements CallReadService {

    private final CallLogRepositoryWrapper callLogRepositoryWrapper;

    @Override
    public CallLogResponse getCallLogByIdentifier(UUID callLogIdentifier) {
        CallLog callLog = callLogRepositoryWrapper.findByIdentifierWithException(callLogIdentifier);
        return CallLogResponse.toCallLogResponse(callLog);
    }

    @Override
    public List<CallLogResponse> getCallLogsByIDs(List<Long> callLogIDs) {
        List<CallLog> callLogs = callLogRepositoryWrapper.findByIdsWithException(callLogIDs);
        return callLogs.stream()
                .map(CallLogResponse::toCallLogResponse)
                .collect(Collectors.toList());
    }
}