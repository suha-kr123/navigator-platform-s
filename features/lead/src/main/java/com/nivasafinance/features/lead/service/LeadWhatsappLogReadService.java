package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadWhatsappLogResponse;

import java.util.UUID;

public interface LeadWhatsappLogReadService {

    PaginatedResponse<LeadWhatsappLogResponse> getWhatsappMessages(
            UUID leadIdentifier,
            LeadDashboardFilters filters,
            PaginationRequest paginationRequest
    );
}
