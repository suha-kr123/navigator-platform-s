package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadDashboardWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LeadReadServiceImpl implements LeadReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final CodeValueMasterService codeValueMasterService;
    private final CodeMasterService codeMasterService;
    private final SourcingChannelReadService sourcingChannelReadService;
    private final LeadDashboardWrapper leadDashboardWrapper;

    public LeadReadServiceImpl(LeadRepositoryWrapper leadRepositoryWrapper,
                                CodeValueMasterService codeValueMasterService,
                                CodeMasterService codeMasterService,
                                SourcingChannelReadService sourcingChannelReadService,
                                LeadDashboardWrapper leadDashboardWrapper) {
        this.leadRepositoryWrapper = leadRepositoryWrapper;
        this.codeValueMasterService = codeValueMasterService;
        this.codeMasterService = codeMasterService;
        this.sourcingChannelReadService = sourcingChannelReadService;
        this.leadDashboardWrapper = leadDashboardWrapper;
    }

    @Override
    @Transactional(readOnly = true)
    public LeadTemplateResponse getLeadTemplate() {
        return LeadTemplateResponse.builder()
                .leadRejectionReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_REJECT_REASON_MASTER, true))
                .leadWithdrawalReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_WITHDRAWAL_REASON_MASTER, true))
                .leadOnholdReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_ONHOLD_REASON_MASTER, true))
                .occupationProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_OCCUPATION_PROFILE_MASTER, true))
                .roofProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_ROOF_PROFILE_MASTER, true))
                .ltvOptions(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_LTV_MASTER, true))
                .foirOptions(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_FOIR_MASTER, true))
                .monthlyIncomes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_MONTHLY_INCOME_MASTER, true))
                .propertyDocumentTypes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PROPERTY_DOCUMENT_TYPE_MASTER, true))
                .locations(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_LOCATION_MASTER, true))
                .bureauRatings(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_BUREAU_RATING_MASTER, true))
                .customerProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_CUSTOMER_PROFILE_MASTER, true))
                .leadPurposes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PURPOSE_MASTER, true
                ))
                .build();
    }

    @Override
    public LeadResponse getLeadByIdentifier(UUID leadIdentifier) {
        return leadRepositoryWrapper.findLeadResponseByIdentifierWithException(leadIdentifier);
    }

    @Override
    public PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        return PreliminaryDetailsResponse.builder()
                .preliminaryDetails(lead.getPreliminaryDetails())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CreditDetailsResponse getCreditDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.CreditRatingDetails creditDetails = lead.getCreditRatingDetails();
        
        if (creditDetails == null) {
            return CreditDetailsResponse.builder().build();
        }
        
        return CreditDetailsResponse.builder()
                .underwriter(creditDetails.getUnderwriter())
                .occupationProfile(creditDetails.getOccupationProfile() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getOccupationProfile()) : null)
                .roofProfile(creditDetails.getRoofProfile() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getRoofProfile()) : null)
                .ltv(creditDetails.getLtv() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getLtv()) : null)
                .foir(creditDetails.getFoir() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getFoir()) : null)
                .monthlyFamilyIncome(creditDetails.getMonthlyFamilyIncome() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getMonthlyFamilyIncome()) : null)
                .propertyDocumentType(creditDetails.getPropertyDocumentType() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getPropertyDocumentType()) : null)
                .eligibleLoanAmount(creditDetails.getEligibleLoanAmount())
                .location(creditDetails.getLocation() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getLocation()) : null)
                .bureauRating(creditDetails.getBureauRating() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getBureauRating()) : null)
                .customerProfiles(creditDetails.getCustomerProfiles() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getCustomerProfiles()) : null)
                .build();
    }

    @Override
    public ProposedDetailsResponse getProposedDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.ProposedDetails proposedDetails = lead.getProposedDetails();
        
        if (proposedDetails == null) {
            return ProposedDetailsResponse.builder().build();
        }
        
        return ProposedDetailsResponse.builder()
                .proposedLoanAmount(proposedDetails.getProposedLoanAmount())
                .roi(proposedDetails.getRoi())
                .tenureValue(proposedDetails.getTenureValue())
                .tenureType(proposedDetails.getTenureType())
                .emi(proposedDetails.getEmi())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        
        if (otherDetails == null || otherDetails.getPropertyDetails() == null) {
            return PropertyDetailsResponse.builder().build();
        }
        
        Lead.PropertyDetails propertyDetails = otherDetails.getPropertyDetails();
        
        return PropertyDetailsResponse.builder()
                .address(propertyDetails.getAddress())
                .geoData(propertyDetails.getGeoData())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SourcingDetailsResponse getSourcingDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        if (lead.getSourcingChannelId() == null) {
            return SourcingDetailsResponse.builder().build();
        }
        
        SourcingChannelResponse sourcingChannelResponse = 
            sourcingChannelReadService.getById(lead.getSourcingChannelId());
        
        return SourcingDetailsResponse.builder()
                .sourcingChannelDetails(sourcingChannelResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DisbursementDetailsResponse getDisbursementDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        
        if (disbursementDetails == null) {
            return DisbursementDetailsResponse.builder().build();
        }
        
        // Map tranches to response
        List<TrancheResponse> trancheResponses = null;
        if (disbursementDetails.getTranches() != null) {
            trancheResponses = disbursementDetails.getTranches().stream()
                    .map(tranche -> TrancheResponse.builder()
                            .identifier(tranche.getIdentifier())
                            .amount(tranche.getAmount())
                            .date(tranche.getDate())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return DisbursementDetailsResponse.builder()
                .disbursedAmount(disbursementDetails.getDisbursedAmount())
                .roi(disbursementDetails.getRoi())
                .tenureValue(disbursementDetails.getTenureValue())
                .tenureType(disbursementDetails.getTenureType())
                .disbursedDate(disbursementDetails.getDisbursedDate())
                .processingFees(disbursementDetails.getProcessingFees())
                .tranches(trancheResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrancheResponse getTrancheByIdentifier(UUID leadIdentifier, UUID trancheIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        
        if (disbursementDetails == null || disbursementDetails.getTranches() == null) {
            throw new RuntimeException("Tranche not found with identifier: " + trancheIdentifier);
        }
        
        // Find tranche by identifier
        Lead.Tranche tranche = disbursementDetails.getTranches().stream()
                .filter(t -> trancheIdentifier.equals(t.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tranche not found with identifier: " + trancheIdentifier));
        
        return TrancheResponse.builder()
                .identifier(tranche.getIdentifier())
                .amount(tranche.getAmount())
                .date(tranche.getDate())
                .build();
    }

    @Override
    public LeadBasicResponse getLeadBasicByIdentifier(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        return LeadBasicResponse.builder().id(lead.getId()).leadIdentifier(leadIdentifier).build();
    }

    @Override
    public PaginatedResponse<LeadDashboardResponse> getLeadDashboard(
            PaginationRequest paginationRequest,
            LeadDashboardFilters filters) {
        return leadDashboardWrapper.findLeadDashboard(paginationRequest, filters);
    }
}
