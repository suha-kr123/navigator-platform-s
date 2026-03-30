package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.externals.customer.lead.service.LeadExternalService;
import com.nivasafinance.features.lead.dto.CurrentCustomerFormStepResponse;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.dto.LeadBREResultResponse;
import com.nivasafinance.features.lead.dto.PatchLeadRequest;
import com.nivasafinance.features.lead.service.LeadBREResultReadService;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.DocumentChecklistResponse;
import com.nivasafinance.features.lead.dto.IncomeObligationDetailsResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.PropertyDetailsResponse;
import com.nivasafinance.features.lead.service.LeadWriteService;
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

    private static final String ELIGIBILITY_CONFIG = "eligibility";

    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;
    private final LeadContactReadService leadContactReadService;
    private final LeadBREResultWriteService leadBREResultWriteService;
    private final LeadBREResultReadService leadBREResultReadService;

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
        return leadBREResultWriteService.executeBre(leadIdentifier, ELIGIBILITY_CONFIG);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeadBREResultResponse> getLatestEligibility(UUID leadIdentifier) {
        return leadBREResultReadService.getResults(leadIdentifier, ELIGIBILITY_CONFIG).stream().findFirst();
    }
}
