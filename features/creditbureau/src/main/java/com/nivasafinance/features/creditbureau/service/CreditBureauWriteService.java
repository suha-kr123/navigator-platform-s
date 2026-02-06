package com.nivasafinance.features.creditbureau.service;

import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.services.creditbureau.dto.CreditBureauPersonData;

import java.util.concurrent.CompletableFuture;

public interface CreditBureauWriteService {
    CreditBureauEnquiryResponse initiateEnquiry(Long personId);
    void linkConsentToEnquiry(Long enquiryId, Long consentId);
    CompletableFuture<CreditBureauEnquiryResponse> executeCreditBureauFlowAsync(CreditBureauEnquiry enquiry, Long personId, CreditBureauPersonData personData);
}

