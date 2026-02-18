package com.nivasafinance.features.creditbureau.service;

import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditBureauReadService {
    Optional<CreditBureauEnquiryResponse> getCbEnquiryById(Long enquiryId);
    CreditBureauEnquiry getCbEnquiryEntityById(Long enquiryId);
    CreditBureauEnquiry getCbEnquiryEntityByIdentifier(UUID identifier);
    List<CustomerEnquiryResponse> getCustomerEnquiryByEnquiryIdentifier(UUID identifier);
    Optional<SummaryResponse> getSummaryByEnquiryIdentifier(UUID enquiryIdentifier);
    Optional<EnquiryStatusResponse> getEnquiryStatusByEnquiryIdentifier(UUID identifier);
    List<TrendsResponse> getTrendsByEnquiryIdentifier(UUID enquiryIdentifier);
    List<DemographicVariationResponse> getDemographicVariationsByEnquiryIdentifier(UUID enquiryIdentifier);
}

