package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.externals.customer.lead.dto.LeadSearchMinimalResponse;
import com.nivasafinance.externals.customer.lead.service.LeadExternalService;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadExternalServiceImpl implements LeadExternalService {

    private static final String EXPERT_SCREENING_STAGE_KEY = "Expert Screening";

    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;
    private final LeadContactReadService leadContactReadService;
    private final LeadEligibilityWriteService leadEligibilityWriteService;
    private final LeadEligibilityReadService leadEligibilityReadService;
    private final LeadStageHistoryWriteService leadStageHistoryWriteService;

    @Override
    public void patchLead(UUID leadIdentifier, PatchLeadRequest request) {
        leadWriteService.patchLead(leadIdentifier, request);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadByIdentifier(UUID leadIdentifier) {
        return leadReadService.getLeadByIdentifier(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentCustomerFormStepResponse getCurrentCustomerFormStep(UUID leadIdentifier) {
        return leadReadService.getCurrentCustomerFormStep(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadContactResponse> getContacts(UUID leadIdentifier) {
        return leadContactReadService.getContacts(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier) {
        return leadReadService.getPropertyDetails(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeObligationDetailsResponse getIncomeObligationDetails(UUID leadIdentifier) {
        return leadReadService.getIncomeObligationDetails(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentChecklistResponse getDocumentChecklist(UUID leadIdentifier) {
        return leadReadService.getDocumentChecklist(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier) {
        return leadReadService.getPreliminaryDetails(leadIdentifier);
    }

    @Override
    public LeadBREResultExecuteResponse executeEligibility(UUID leadIdentifier) {
        return leadEligibilityWriteService.executeEligibility(leadIdentifier);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeadEligibilityResponse> getLatestEligibility(UUID leadIdentifier) {
        return leadEligibilityReadService.getLatestEligibility(leadIdentifier);
    }

    @Override
    public LeadStageHistoryResponse transitionToExpertScreening(UUID leadIdentifier) {
        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey(EXPERT_SCREENING_STAGE_KEY)
                .build();
        return LeadStageHistoryResponse.from(
                leadStageHistoryWriteService.createStageEntry(leadIdentifier, request));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LeadSearchMinimalResponse> searchLeads(
            PaginationRequest paginationRequest, LeadSearchRequest request) {
        PaginatedResponse<LeadSearchResponse> result =
                leadReadService.searchLeads(paginationRequest, request);
        List<LeadSearchMinimalResponse> minimalResults = result.getContent().stream()
                .map(r -> LeadSearchMinimalResponse.builder()
                        .leadIdentifier(r.getLeadIdentifier())
                        .primaryPersonName(r.getPrimaryPersonName())
                        .status(r.getStatus())
                        .subStatus(r.getSubStatus())
                        .build())
                .toList();
        return PaginatedResponse.<LeadSearchMinimalResponse>builder()
                .content(minimalResults)
                .pagination(result.getPagination())
                .build();
    }
}
