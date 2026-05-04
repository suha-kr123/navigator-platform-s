package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappLogResponse;

import java.util.UUID;

public interface AdvisorWhatsappLogReadService {

    PaginatedResponse<AdvisorWhatsappLogResponse> getWhatsappMessages(
            UUID advisorIdentifier,
            AdvisorDashboardFilters filters,
            PaginationRequest paginationRequest
    );
}
