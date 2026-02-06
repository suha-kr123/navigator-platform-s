package com.nivasafinance.features.person.service;

import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;

import java.util.UUID;

/**
 * Service for managing credit bureau operations at person level.
 * Provides methods to initiate and manage credit bureau enquiries for persons.
 */
public interface PersonCreditBureauService {

    /**
     * Initiates credit bureau enquiry for a person. Creates enquiry (INITIATED), sends consent link via
     * createAndSendConsent; does not trigger the pull. The pull runs when the user accepts via the accept API.
     *
     * @param request The credit bureau enquiry request; must include leadIdentifier and contactIdentifier when
     *                called from Lead (for the consent link).
     * @return CreditBureauEnquiryResponse containing enquiry id, identifier, and status
     */
    CreditBureauEnquiryResponse initiateCreditBureauEnquiry(CreditBureauEnquiryRequest request);

    /**
     * Called after consent is accepted. Links consent to enquiry, updates person.consent_details, and
     * triggers the async credit bureau pull.
     *
     * @param consentId        the accepted consent id
     * @param enquiryIdentifier the enquiry identifier (UUID)
     */
    void onConsentGranted(Long consentId, UUID enquiryIdentifier);
}
