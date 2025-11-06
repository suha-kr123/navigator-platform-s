package com.nivasafinance.features.leadstages.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;

public interface LeadStageHistoryReadService {
    
    PaginatedResponse<LeadStageHistoryResponse> getStageHistoryByLeadId(Long leadId, PaginationRequest paginationRequest);
}

