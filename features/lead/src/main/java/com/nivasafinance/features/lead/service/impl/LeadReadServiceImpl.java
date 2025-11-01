package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.CreditDetailsResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.PropertyDetailsResponse;
import com.nivasafinance.features.lead.dto.ProposedDetailsResponse;
import com.nivasafinance.features.lead.dto.SourcingDetailsResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LeadReadServiceImpl implements LeadReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final CodeValueMasterService codeValueMasterService;
    private final SourcingChannelReadService sourcingChannelReadService;

    public LeadReadServiceImpl(LeadRepositoryWrapper leadRepositoryWrapper,
                                CodeValueMasterService codeValueMasterService,
                                SourcingChannelReadService sourcingChannelReadService) {
        this.leadRepositoryWrapper = leadRepositoryWrapper;
        this.codeValueMasterService = codeValueMasterService;
        this.sourcingChannelReadService = sourcingChannelReadService;
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
                .eligibleLoanAmount(creditDetails.getEligibleLoanAmount() != null 
                    ? codeValueMasterService.getByKey(creditDetails.getEligibleLoanAmount()) : null)
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
}
