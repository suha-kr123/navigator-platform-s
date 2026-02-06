package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.features.consent.dto.AcceptConsentRequest;
import com.nivasafinance.features.consent.dto.WithdrawConsentRequest;
import com.nivasafinance.features.consent.service.ConsentReadService;
import com.nivasafinance.features.consent.service.ConsentWriteService;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.lead.dto.InitiateCbEnquiryResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.service.PersonCreditBureauService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadCreditBureauWriteServiceImpl implements LeadCreditBureauWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final PersonReadService personReadService;
    private final PersonCreditBureauService personCreditBureauService;
    private final ConsentReadService consentReadService;
    private final ConsentWriteService consentWriteService;
    private final CreditBureauReadService creditBureauReadService;

    @Override
    public InitiateCbEnquiryResponse initiateEnquiry(UUID leadIdentifier, UUID contactIdentifier) {
        // Validate lead and contact exist
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        
        // Validate contact belongs to lead
        validateContactBelongsToLead(lead, contact.getId());
        
        // Get person from contact
        PersonResponse personResponse = personReadService.getPersonById(contact.getPersonId());

        // Build request DTO
        CreditBureauEnquiryRequest request = CreditBureauEnquiryRequest.builder()
                .personId(personResponse.getId())
                .entityType(SystemEntities.LEAD.name())
                .entityId(lead.getId())
                .businessPurpose("Initiate credit bureau enquiry for contact: " + contactIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .build();

        // Call person service to initiate enquiry (handles all internal logic)
        CreditBureauEnquiryResponse enquiryResponse =
            personCreditBureauService.initiateCreditBureauEnquiry(request);
        
        // Update contact with enquiry ID
        updateContactCbEnquiryId(contact, enquiryResponse.getId());

        return InitiateCbEnquiryResponse.builder()
                .enquiryIdentifier(enquiryResponse.getIdentifier())
                .status(enquiryResponse.getStatus() != null ? enquiryResponse.getStatus() : CreditBureauEnquiryStatus.INITIATED)
                .consentIdentifier(enquiryResponse.getConsentIdentifier())
                .build();
    }

    @Override
    public void acceptConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }

        Long consentId = consentReadService.findByIdentifierWithException(consentIdentifier).getId();

        // Get person to get phone number for withdrawal link
        PersonResponse personResponse = personReadService.getPersonById(contact.getPersonId());
        String recipientPhone = null;
        if (personResponse.getMobileNumbers() != null && !personResponse.getMobileNumbers().isEmpty()) {
            recipientPhone = personResponse.getMobileNumbers().stream()
                    .map(MobileNumberDetails::getNumber)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        consentWriteService.acceptConsent(AcceptConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .personId(personResponse.getId())
                .recipientPhone(recipientPhone)
                .build());
        personCreditBureauService.onConsentGranted(consentId, enquiryIdentifier);
    }

    @Override
    public void withdrawConsent(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier, UUID consentIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if  (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }
        consentWriteService.withdrawConsent(WithdrawConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .build());
    }

    private void validateContactBelongsToLead(Lead lead, Long contactId) {
        List<Long> contacts = lead.getContacts();
        if (CollectionUtils.isEmpty(contacts) || !contacts.contains(contactId)) {
            throw new BadRequestException("Contact does not belong to the provided lead identifier");
        }
    }

    private void updateContactCbEnquiryId(Contact contact, Long enquiryId) {
        // Update contact's cb_enquiry_id array
        List<Long> contactEnquiryIds = contact.getCbEnquiryId();
        if (contactEnquiryIds == null) {
            contactEnquiryIds = new ArrayList<>();
        }
        if (!contactEnquiryIds.contains(enquiryId)) {
            contactEnquiryIds.add(enquiryId);
            contact.setCbEnquiryId(contactEnquiryIds);
            contactRepositoryWrapper.saveWithException(contact);
        }
    }
}

