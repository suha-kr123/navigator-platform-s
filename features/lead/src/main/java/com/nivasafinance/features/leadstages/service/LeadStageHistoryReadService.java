package com.nivasafinance.features.leadstages.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;

import java.util.UUID;

public interface LeadStageHistoryReadService {
    
    PaginatedResponse<LeadStageHistoryResponse> getStageHistoryByLeadId(UUID leadId, PaginationRequest paginationRequest);
}

