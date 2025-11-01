package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.CreditDetailsResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LeadReadServiceImpl implements LeadReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final CodeValueMasterService codeValueMasterService;

    public LeadReadServiceImpl(LeadRepositoryWrapper leadRepositoryWrapper,
                                CodeValueMasterService codeValueMasterService) {
        this.leadRepositoryWrapper = leadRepositoryWrapper;
        this.codeValueMasterService = codeValueMasterService;
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
}
