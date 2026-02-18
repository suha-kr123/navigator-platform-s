package com.nivasafinance.externals.creditbureau.service;

import com.nivasafinance.externals.creditbureau.dto.ContactCreditBureauResponse;
import com.nivasafinance.features.consent.dto.AcceptConsentResponse;
import com.nivasafinance.features.consent.dto.WithdrawConsentResponse;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;

import java.util.Optional;
import java.util.UUID;

public interface CreditBureauExternalService {

    ContactCreditBureauResponse getContactByLeadAndContact(UUID leadIdentifier, UUID contactIdentifier);

    Optional<EnquiryConsentStatusResponse> getConsentStatus(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier);

    AcceptConsentResponse acceptConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier);

    WithdrawConsentResponse withdrawConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier);
}
