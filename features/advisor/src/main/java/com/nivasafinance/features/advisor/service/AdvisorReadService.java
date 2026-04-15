package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.entity.Advisor;

import java.util.Optional;
import java.util.UUID;

public interface AdvisorReadService {

    Optional<Advisor> findAdvisorByUsername(String username);

    Optional<Advisor> findAdvisorByUsernameIncludingDeleted(String username);

    Advisor findAdvisorByIdentifierIncludingDeleted(UUID identifier);

    AdvisorResponse getAdvisorByIdentifier(UUID identifier);

    SourcingDetailsResponse getSourcingDetails(UUID identifier);

    AdvisorTemplateResponse getAdvisorTemplate();

    PaginatedResponse<AdvisorBasicResponse> getAllAdvisors(PaginationRequest paginationRequest, String name, String mobileNumber);

    PaginatedResponse<AdvisorBasicResponse> searchAdvisors(PaginationRequest paginationRequest, AdvisorSearchRequest request);

    /**
     * Find an advisor by mobile number without office filtering.
     * For use in flows where staff context is not available (e.g. Exotel webhooks).
     */
    Optional<AdvisorBasicResponse> findAdvisorByMobileNo(String mobileNumber);

    PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
        PaginationRequest paginationRequest,
        AdvisorDashboardFilters filters);

    // advisors where the current user is the owner
    PaginatedResponse<AdvisorBasicResponse> getMyAdvisors(PaginationRequest paginationRequest);

    // advisors by the given referral code
    PaginatedResponse<AdvisorBasicResponse> getAdvisorsByReferralCode(String referralCode, PaginationRequest paginationRequest);

    PaginatedResponse<AdminAdvisorBasicResponse> adminSearchAdvisors(PaginationRequest paginationRequest, AdminAdvisorSearchRequest request);

    PaginatedResponse<AdminAdvisorBasicResponse> getDeletedAdvisors(PaginationRequest paginationRequest);

}
