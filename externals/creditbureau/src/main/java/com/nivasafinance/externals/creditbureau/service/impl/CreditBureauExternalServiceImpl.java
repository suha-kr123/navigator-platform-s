package com.nivasafinance.externals.creditbureau.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.externals.creditbureau.dto.ContactCreditBureauResponse;
import com.nivasafinance.externals.creditbureau.service.CreditBureauExternalService;
import com.nivasafinance.features.consent.dto.AcceptConsentResponse;
import com.nivasafinance.features.consent.dto.WithdrawConsentResponse;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import com.nivasafinance.features.lead.dto.LeadContactPersonDetails;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadCreditBureauReadService;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditBureauExternalServiceImpl implements CreditBureauExternalService {

    private final LeadContactReadService leadContactReadService;
    private final LeadCreditBureauReadService leadCreditBureauReadService;
    private final LeadCreditBureauWriteService leadCreditBureauWriteService;

    @Override
    @Transactional(readOnly = true)
    public ContactCreditBureauResponse getContactByLeadAndContact(UUID leadIdentifier, UUID contactIdentifier) {
        LeadContactResponse contactResponse = leadContactReadService.getContactById(leadIdentifier, contactIdentifier);
        LeadContactPersonDetails personDetails = contactResponse.getContactPersonDetails();

        List<AddressData> addresses = leadContactReadService.getAddresses(contactIdentifier);
        List<IdentifierData> identifiers = leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier);

        return ContactCreditBureauResponse.builder()
                .firstName(personDetails.getFirstName())
                .lastName(personDetails.getLastName())
                .mobileNumberDetails(personDetails.getMobileNumbers() != null ? personDetails.getMobileNumbers() : Collections.emptyList())
                .identifierData(identifiers != null ? identifiers : Collections.emptyList())
                .dateOfBirth(personDetails.getDateOfBirth())
                .address(addresses != null ? addresses : Collections.emptyList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnquiryConsentStatusResponse> getConsentStatus(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        return leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
    }

    @Override
    @Transactional
    public AcceptConsentResponse acceptConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier) {
        leadCreditBureauWriteService.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        return AcceptConsentResponse.builder()
                .consentStatus(ConsentStatus.RECEIVED)
                .build();
    }

    @Override
    @Transactional
    public WithdrawConsentResponse withdrawConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier) {
        leadCreditBureauWriteService.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        return WithdrawConsentResponse.builder()
                .consentStatus(ConsentStatus.REQUEST_FOR_WITHDRAWAL)
                .build();
    }
}
