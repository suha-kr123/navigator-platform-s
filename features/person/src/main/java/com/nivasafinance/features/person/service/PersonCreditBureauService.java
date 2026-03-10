package com.nivasafinance.features.person.service;

import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.person.dto.RecordCbConsentResult;

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
     * @param request The credit bureau enquiry request containing personId, entityType, entityId, and businessPurpose;
     *                must include leadIdentifier and contactIdentifier when called from Lead (for the consent link).
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

    /**
     * Records CB consent at person level (creates consent as RECEIVED and updates person.consent_details).
     * No enquiry is created. Used for customer web journey where consent is taken before fetch report.
     *
     * @param personId the person ID
     * @return result containing the consent identifier
     */
    RecordCbConsentResult recordCbConsentReceived(Long personId);
}
