package com.nivasafinance.features.advisor.service.crm;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.*;

import java.util.UUID;

public interface AdvisorCRMService {

    AdvisorTemplateResponse getAdvisorTemplate();

    PaginatedResponse<AdvisorBasicResponse> getAllAdvisors(PaginationRequest paginationRequest, String name, String mobileNumber);

    UUID createAdvisor(CreateAdvisorRequest request);

    AdvisorResponse getAdvisorByIdentifier(UUID identifier);

    PaginatedResponse<AdvisorBasicResponse> searchAdvisors(PaginationRequest paginationRequest, AdvisorSearchRequest request);

    void updateAdvisor(UUID identifier, UpdateAdvisorRequest request);

    void updateQualificationDetails(UUID identifier, UpdateQualificationDetailsRequest request);

    void updateOccupationDetails(UUID identifier, UpdateOccupationDetailsRequest request);

    void updateSegmentationDetails(UUID identifier, UpdateSegmentationDetailsRequest request);

    void rejectAdvisor(UUID identifier, RejectAdvisorRequest request);

    void dormantAdvisor(UUID identifier, DormantAdvisorRequest request);

    void activateAdvisor(UUID identifier);

    void outOfGeoAdvisor(UUID identifier, OutOfGeoAdvisorRequest request);

    void undoRejectAdvisor(UUID identifier);

    void undoDormantAdvisor(UUID identifier);

    void undoOutOfGeoAdvisor(UUID identifier);

    PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
            PaginationRequest paginationRequest,
            AdvisorDashboardFilters filters);

    PaginatedResponse<AdvisorBasicResponse> getMyAdvisors(PaginationRequest paginationRequest);

    PaginatedResponse<AdvisorBasicResponse> getAdvisorsByReferralCode(String referralCode, PaginationRequest paginationRequest);
}
