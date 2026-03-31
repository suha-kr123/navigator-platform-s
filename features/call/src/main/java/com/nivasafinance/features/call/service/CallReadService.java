package com.nivasafinance.features.call.service;

import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallReadService {
    CallLogResponse getCallLogByIdentifier(UUID callLogIdentifier);
    List<CallLogResponse> getCallLogsByIDs(List<Long> callLogIDs);
    CallLogResponse getCallLogByID(Long callLogID);
    Optional<CallLogResponse> getCallLogByProviderId(String providerId);
    Optional<Long> findLeadIdByCallLogId(Long callLogId);
    Page<CallLog> findByProviderAndCreatedAtRange(CallProvider provider, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
