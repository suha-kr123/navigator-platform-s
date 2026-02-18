package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.referral.enums.EntityType;

import java.util.List;
import java.util.UUID;

public interface LeadReadService {
    LeadTemplateResponse getLeadTemplate();
    LeadResponse getLeadByIdentifier(UUID leadIdentifier);
    PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier);
    CreditDetailsResponse getCreditDetails(UUID leadIdentifier);
    ProposedDetailsResponse getProposedDetails(UUID leadIdentifier);
    PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier);
    SourcingDetailsResponse getSourcingDetails(UUID leadIdentifier);
    DisbursementDetailsResponse getDisbursementDetails(UUID leadIdentifier);
    TrancheResponse getTrancheByIdentifier(UUID leadIdentifier, UUID trancheIdentifier);
    LeadBasicResponse getLeadBasicByIdentifier(UUID leadIdentifier);
    PaginatedResponse<LeadDashboardResponse> getLeadDashboard(
            PaginationRequest paginationRequest,
            LeadDashboardFilters filters);
    PaginatedResponse<LeadSearchResponse> searchLeads(PaginationRequest paginationRequest, LeadSearchRequest request);
    LeadDashboardFiltersResponse getLeadDashboardFilters(LeadDashboardFiltersFilters filters);
    List<LeadWorkflowDetailsDto> findLeadsByPersonIdsAndStatusesAndSubstatuses(List<Long> personIds,
        List<LeadStatus> statuses, List<LeadSubStatus> substatuses);
    
    LeadBasicResponse getLeadByReferralTrackingCode(String referralTrackingCode);

    PaginatedResponse<LeadBasicResponse> getLeadsByEntity(EntityType entityType, UUID entityIdentifier, PaginationRequest paginationRequest);

    PaginatedResponse<LeadBasicResponse> getLeadsByReferralCode(String referralCode, PaginationRequest paginationRequest);
}
