package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.*;

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
}
