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

    PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
        PaginationRequest paginationRequest,
        AdvisorDashboardFilters filters);

    // advisors where the current user is the owner
    PaginatedResponse<AdvisorBasicResponse> getMyAdvisors(PaginationRequest paginationRequest);

    // advisors by the given referral code
    PaginatedResponse<AdvisorBasicResponse> getAdvisorsByReferralCode(String referralCode, PaginationRequest paginationRequest);

}
