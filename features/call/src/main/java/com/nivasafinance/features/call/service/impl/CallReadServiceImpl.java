package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.repository.CallLogRepository;
import com.nivasafinance.features.call.repository.CallLogRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CallReadServiceImpl implements CallReadService {

    private final CallLogRepositoryWrapper callLogRepositoryWrapper;
    private final CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;
    private final CallLogRepository callLogRepository;

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

    @Override
    public CallLogResponse getCallLogByID(Long callLogID) {
        return CallLogResponse.toCallLogResponse(callLogRepositoryWrapper.findByIdWithException(callLogID));
    }

    @Override
    public Optional<CallLogResponse> getCallLogByProviderId(String providerId) {
        return callLogRepository.findByProviderId(providerId)
                .map(CallLogResponse::toCallLogResponse);
    }

    @Override
    public Optional<Long> findLeadIdByCallLogId(Long callLogId) {
        return callLogLeadRepositoryWrapper.findByCallLogId(callLogId)
                .map(mapping -> mapping.getLeadId());
    }

    @Override
    public Page<CallLog> findByProviderAndCreatedAtRange(CallProvider provider, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return callLogRepository.findByProviderAndCreatedAtRange(provider, start, end, pageable);
    }
}