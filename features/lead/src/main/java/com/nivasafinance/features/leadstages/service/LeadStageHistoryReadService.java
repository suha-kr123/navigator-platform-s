package com.nivasafinance.features.leadstages.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryDisplayResponse;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface LeadStageHistoryReadService {

    PaginatedResponse<LeadStageHistoryResponse> getStageHistoryByLeadId(UUID leadId, PaginationRequest paginationRequest);

    /**
     * Returns stage history for the lead with external display labels (from n_stage_config).
     */
    List<LeadStageHistoryDisplayResponse> getStageHistoryWithDisplayLabelsByLeadId(UUID leadId);
}

