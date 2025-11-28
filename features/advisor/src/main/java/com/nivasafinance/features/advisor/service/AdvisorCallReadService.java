package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorCallLogResponse;

import java.util.UUID;

public interface AdvisorCallReadService {
    PaginatedResponse<AdvisorCallLogResponse> getCallLogs(UUID advisorIdentifier, PaginationRequest paginationRequest);
}
