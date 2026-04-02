package com.nivasafinance.features.creditbureau.service.impl;

import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.creditbureau.repository.*;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CreditBureauReadServiceImpl implements CreditBureauReadService {

    private final CreditBureauRepositoryWrapper creditBureauRepositoryWrapper;
    private final CreditBureauRepository creditBureauRepository;
    private final CreditBureauCustomerEnquiryRepositoryWrapper creditBureauCustomerEnquiryRepositoryWrapper;
    private final CreditBureauSummaryRepositoryWrapper creditBureauSummaryRepositoryWrapper;
    private final CreditBureauTrendsRepositoryWrapper creditBureauTrendsRepositoryWrapper;
    private final CreditBureauDemographicVariationRepositoryWrapper creditBureauDemographicVariationRepositoryWrapper;

    @Override
    public Optional<CreditBureauEnquiryResponse> getCbEnquiryById(Long enquiryId) {
        return creditBureauRepository.findById(enquiryId)
                .map(CreditBureauEnquiryResponse::toCbEnquiryResponse);
    }

    @Override
    public CreditBureauEnquiry getCbEnquiryEntityById(Long enquiryId) {
        return creditBureauRepositoryWrapper.findByIdWithException(enquiryId);
    }

    @Override
    public CreditBureauEnquiry getCbEnquiryEntityByIdentifier(UUID identifier) {
        return creditBureauRepositoryWrapper.findByIdentifierWithException(identifier);
    }

    @Override
    public List<CustomerEnquiryResponse> getCustomerEnquiryByEnquiryIdentifier(UUID enquiryIdentifier) {
        CreditBureauEnquiry enquiry = creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)
                .orElseThrow(() -> new RuntimeException("Credit bureau enquiry not found with identifier: " + enquiryIdentifier));

        return creditBureauCustomerEnquiryRepositoryWrapper.findByEnquiryId(enquiry.getId())
                .stream()
                .map(CustomerEnquiryResponse::toCustomerEnquiryResponse)
                .collect(Collectors.toList());
    }

    @Override 
    public Optional<SummaryResponse> getSummaryByEnquiryIdentifier(UUID enquiryIdentifier){
        CreditBureauEnquiry enquiry = creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)
                .orElseThrow(() -> new RuntimeException("Credit bureau enquiry not found for identifier: " + enquiryIdentifier));

        return creditBureauSummaryRepositoryWrapper.findByEnquiryId(enquiry.getId())
                .map(SummaryResponse::toSummaryResponse);

        }

    @Override
    public Optional<EnquiryStatusResponse> getEnquiryStatusByEnquiryIdentifier(UUID enquiryIdentifier){
        CreditBureauEnquiry enquiry = creditBureauRepositoryWrapper.findByIdentifier((enquiryIdentifier))
                .orElseThrow(() -> new RuntimeException("Credit bureau enquiry not found with identifier: " + enquiryIdentifier));
        return Optional.of(EnquiryStatusResponse.toStatusResponse(enquiry));
    }

    @Override
    public List<TrendsResponse> getTrendsByEnquiryIdentifier(UUID enquiryIdentifier) {
        CreditBureauEnquiry enquiry = creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)
                .orElseThrow(() -> new RuntimeException("Credit bureau enquiry not found with identifier: " + enquiryIdentifier));

        return creditBureauTrendsRepositoryWrapper.findByEnquiryIdOrderByDateDesc(enquiry.getId());
    }

    @Override
    public List<DemographicVariationResponse> getDemographicVariationsByEnquiryIdentifier(UUID enquiryIdentifier) {
        CreditBureauEnquiry enquiry = creditBureauRepositoryWrapper.findByIdentifier(enquiryIdentifier)
                .orElseThrow(() -> new RuntimeException("Credit bureau enquiry not found with identifier: " + enquiryIdentifier));

        return creditBureauDemographicVariationRepositoryWrapper.findByEnquiryIdAsResponse(enquiry.getId());
    }

    @Override
    public List<DemographicVariationResponse> getDemographicVariationsByEnquiryId(Long enquiryId) {
        return creditBureauDemographicVariationRepositoryWrapper.findByEnquiryIdAsResponse(enquiryId);
    }
}

