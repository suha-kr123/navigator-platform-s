package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.lead.dto.LeadContactPersonDetails;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.entity.Applicant;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.service.PersonReadService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class LeadContactReadServiceImpl implements LeadContactReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final ApplicantRepositoryWrapper applicantRepositoryWrapper;
    private final PersonReadService personReadService;


    @Override
    public List<LeadContactResponse> getContacts(UUID leadId) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        List<LeadContactResponse> responses = new ArrayList<>();

        if (lead.getContacts() == null) {
            return responses;
        }

        for (Long contactId : lead.getContacts()) {
            Contact contact = contactRepositoryWrapper.findByIdWithException(contactId);
            LeadContactResponse response = mapToContactResponse(lead, contact);
            responses.add(response);
        }

        return responses;
    }

    @Override
    public LeadContactResponse getContactById(UUID leadId, UUID contactId) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactId);
        return mapToContactResponse(lead, contact);
    }

    private LeadContactResponse mapToContactResponse(Lead lead, Contact contact) {
        // Get person details
        PersonResponse person = personReadService.getPersonById(contact.getPersonId());

        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .mobileNumbers(person.getMobileNumbers())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .build();

        // Determine applicant type
        LeadContactPersonType applicantType = determineApplicantType(lead, contact);

        return LeadContactResponse.builder()
                .identifier(contact.getIdentifier())
                .contactPersonDetails(personDetails)
                .applicantType(applicantType)
                .isDecisionMaker(contact.getIsDecisionMaker())
                .isPropertyOwner(contact.getIsPropertyOwner())
                .build();
    }

    private LeadContactPersonType determineApplicantType(Lead lead, Contact contact) {
        // Check if applicant
        if (lead.getApplicant() != null) {
            Applicant applicant = applicantRepositoryWrapper.findByIdWithException(lead.getApplicant());
            if (applicant.getPersonId().equals(contact.getPersonId())) {
                return LeadContactPersonType.APPLICANT;
            }
        }

        // Check if co-applicant
        if (lead.getCoApplicants() != null) {
            for (Long coApplicantId : lead.getCoApplicants()) {
                Applicant coApplicant = applicantRepositoryWrapper.findByIdWithException(coApplicantId);
                if (coApplicant.getPersonId().equals(contact.getPersonId())) {
                    return LeadContactPersonType.CO_APPLICANT;
                }
            }
        }

        return LeadContactPersonType.NONE;
    }

    private Contact findContactByIdentifier(Lead lead, UUID contactIdentifier) {
        if (lead.getContacts() == null) {
            throw new RuntimeException("Contact not found with identifier: " + contactIdentifier);
        }

        for (Long contactId : lead.getContacts()) {
            Contact contact = contactRepositoryWrapper.findByIdWithException(contactId);
            if (contact.getIdentifier().equals(contactIdentifier)) {
                return contact;
            }
        }

        throw new RuntimeException("Contact not found with identifier: " + contactIdentifier);
    }

    @Override
    public List<AddressData> getAddresses(UUID contactIdentifier) {
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        return personReadService.getAddresses(contact.getPersonId());
    }

    @Override
    public AddressData getAddress(UUID contactIdentifier, String addressId) {
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        try {
            return personReadService.getAddress(contact.getPersonId(), addressId);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found for contact");
        }
    }

    @Override
    public List<IdentifierData> getIdentifiers(UUID leadId, UUID contactIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactIdentifier);
        return personReadService.getIdentifiers(contact.getPersonId());
    }

    @Override
    public IdentifierData getIdentifier(UUID leadId, UUID contactIdentifier, UUID identifierId) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactIdentifier);
        try {
            return personReadService.getIdentifier(contact.getPersonId(), identifierId);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Identifier not found for contact");
        }
    }

}

