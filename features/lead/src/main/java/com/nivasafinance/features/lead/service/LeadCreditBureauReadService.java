package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadCreditBureauReadService {
    List<CustomerEnquiryResponse> getCustomerEnquiryByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
    Optional<SummaryResponse> getSummaryResponseByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
    Optional<EnquiryStatusResponse> getEnquiryStatusByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
    Optional<EnquiryConsentStatusResponse> getConsentStatusByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
    List<TrendsResponse> getScoreTrendsByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
    List<DemographicVariationResponse> getDemographicVariationsByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
}
