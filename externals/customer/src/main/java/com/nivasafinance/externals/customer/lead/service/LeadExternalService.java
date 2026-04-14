package com.nivasafinance.externals.customer.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.externals.customer.lead.dto.LeadSearchMinimalResponse;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadExternalService {

    void patchLead(UUID leadIdentifier, PatchLeadRequest request);

    LeadResponse getLeadByIdentifier(UUID leadIdentifier);

    CurrentCustomerFormStepResponse getCurrentCustomerFormStep(UUID leadIdentifier);

    List<LeadContactResponse> getContacts(UUID leadIdentifier);

    PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier);

    IncomeObligationDetailsResponse getIncomeObligationDetails(UUID leadIdentifier);

    DocumentChecklistResponse getDocumentChecklist(UUID leadIdentifier);

    PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier);

    LeadBREResultExecuteResponse executeEligibility(UUID leadIdentifier);

    Optional<LeadEligibilityResponse> getLatestEligibility(UUID leadIdentifier);

    LeadStageHistoryResponse transitionToExpertScreening(UUID leadIdentifier);

    PaginatedResponse<LeadSearchMinimalResponse> searchLeads(PaginationRequest paginationRequest, LeadSearchRequest request);
}
