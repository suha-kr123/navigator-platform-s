package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadCallLogResponse;
import com.nivasafinance.features.lead.dto.LeadCallSummaryResponse;

import java.util.UUID;

public interface LeadCallReadService {
    PaginatedResponse<LeadCallLogResponse> getCallLogs(UUID leadIdentifier, PaginationRequest paginationRequest, boolean hasAiAnalysis);

    LeadCallSummaryResponse getCallSummary(UUID leadIdentifier);

    /**
     * Recomputes {@code Lead.callSummaryDetails} from linked call logs and returns the fresh snapshot.
     */
    LeadCallSummaryResponse refreshCallSummary(UUID leadIdentifier);

    /**
     * Recomputes denormalized {@code Lead.callSummaryDetails} from linked call logs ({@code n_call_log_lead}).
     */
    void recalculateLeadCallSummary(Long leadId);
}
