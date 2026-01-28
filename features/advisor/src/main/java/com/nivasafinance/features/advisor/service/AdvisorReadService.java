package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.*;

import java.util.UUID;

public interface AdvisorReadService {

    AdvisorResponse getAdvisorByIdentifier(UUID identifier);

    SourcingDetailsResponse getSourcingDetails(UUID identifier);

    AdvisorTemplateResponse getAdvisorTemplate();

    PaginatedResponse<AdvisorBasicResponse> getAllAdvisors(PaginationRequest paginationRequest, String name, String mobileNumber);

    PaginatedResponse<AdvisorBasicResponse> searchAdvisors(PaginationRequest paginationRequest, AdvisorSearchRequest request);

    PaginatedResponse<AdvisorLeadResponse> getLeadsByAdvisorId(UUID advisorId, PaginationRequest paginationRequest);

    PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
            PaginationRequest paginationRequest,
            AdvisorDashboardFilters filters);

    PaginatedResponse<AdvisorBasicResponse> getMyAdvisors(PaginationRequest paginationRequest);
}
