package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorSearchResponse;
import com.nivasafinance.features.advisor.dto.AdvisorTemplateResponse;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;

import java.util.UUID;

public interface AdvisorReadService {
    
    AdvisorResponse getAdvisorByIdentifier(UUID identifier);
    
    SourcingDetailsResponse getSourcingDetails(UUID identifier);
    
    AdvisorTemplateResponse getAdvisorTemplate();
    
    PaginatedResponse<AdvisorSearchResponse> searchAdvisors(PaginationRequest paginationRequest, AdvisorSearchRequest request);
    
    PaginatedResponse<LeadBasicResponse> getLeadsByAdvisorId(UUID advisorId, PaginationRequest paginationRequest);
}
