package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.InitiateCbEnquiryResponse;
import com.nivasafinance.features.lead.dto.RecordCbConsentResponse;

import java.util.UUID;

public interface LeadCreditBureauWriteService {
    InitiateCbEnquiryResponse initiateEnquiry(UUID leadIdentifier, UUID contactIdentifier);

    RecordCbConsentResponse recordCbConsentReceived(UUID leadIdentifier, UUID contactIdentifier);

    void acceptConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier);

    void resendConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier);

    void withdrawConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier);

    void regenerateCbReport(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);
}

