package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadContactCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadContactDeletionEventPayload;
import com.nivasafinance.common.events.payload.LeadContactUpdationEventPayload;
import com.nivasafinance.features.lead.dto.BulkContactsUpdateRequest;
import com.nivasafinance.features.lead.dto.BulkContactsUpdateResponse;
import com.nivasafinance.features.lead.dto.LeadContactPersonDetails;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.CreateLeadContactRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadContactRequest;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.entity.Applicant;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class LeadContactWriteServiceImpl implements LeadContactWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final ApplicantRepositoryWrapper applicantRepositoryWrapper;
    private final PersonWriteService personWriteService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LeadContactReadService leadContactReadService;

    @Override
    @Transactional
    public void createContact(UUID leadId, CreateLeadContactRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);

        // Create Person
        PersonCreateRequest personRequest = mapToPersonCreateRequest(request.getContactPersonDetails());
        PersonCreateResponse personResponse = personWriteService.createPerson(personRequest);

        // Create Contact
        Contact contact = new Contact();
        contact.setPersonId(personResponse.getId());
        contact.setIsDecisionMaker(request.getIsDecisionMaker());
        contact.setIsPropertyOwner(request.getIsPropertyOwner());
        Contact savedContact = contactRepositoryWrapper.saveWithException(contact);

        // Handle decision maker logic
        if (request.getIsDecisionMaker()) {
            unsetOtherDecisionMakers(lead, savedContact.getId());
        }

        // Add contact to lead
        addContactToLead(lead, savedContact.getId());

        // Handle applicant type
        handleApplicantType(lead, savedContact, request.getApplicantType());

        // Update primary contact ID
        updatePrimaryContactId(lead);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadContactCreatedEvent(lead, savedContact, request.getApplicantType());
    }

    @Override
    @Transactional
    public void updateContact(UUID leadId, UUID contactId, UpdateLeadContactRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactId);

        // Update Person
        PersonUpdateRequest personRequest = mapToPersonUpdateRequest(request.getContactPersonDetails());
        personWriteService.updatePerson(contact.getPersonId(), personRequest);

        // Determine current applicant type
        LeadContactPersonType currentType = determineCurrentApplicantType(lead, contact);

        // Update contact details
        contact.setIsDecisionMaker(request.getIsDecisionMaker());
        contact.setIsPropertyOwner(request.getIsPropertyOwner());
        contactRepositoryWrapper.saveWithException(contact);

        // Handle decision maker logic
        if (request.getIsDecisionMaker()) {
            unsetOtherDecisionMakers(lead, contact.getId());
        }

        // Handle applicant type changes
        if (currentType != request.getApplicantType()) {
            // Remove old applicant type
            removeApplicantType(lead, contact, currentType);
            // Add new applicant type
            handleApplicantType(lead, contact, request.getApplicantType());
        }

        // Update primary contact ID
        updatePrimaryContactId(lead);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadContactUpdatedEvent(lead, contact, request.getApplicantType());
    }

    @Override
    @Transactional
    public void deleteContact(UUID leadId, UUID contactId) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);

        // At least one contact should exist after deleting
        if (lead.getContacts() == null || lead.getContacts().size() < 2) {
            throw new RuntimeException("Cannot delete contact. At least one contact must remain for the lead.");
        }

        Contact contact = findContactByIdentifier(lead, contactId);

        // Determine and remove applicant type
        LeadContactPersonType currentType = determineCurrentApplicantType(lead, contact);

        // Publish event before removing
        publishLeadContactDeletedEvent(lead, contact, currentType);

        removeApplicantType(lead, contact, currentType);

        // Remove contact from lead
        removeContactFromLead(lead, contact.getId());

        // Update primary contact ID
        updatePrimaryContactId(lead);

        // For now, we'll keep the person entity as it might be used elsewhere

        leadRepositoryWrapper.saveWithException(lead);
    }

    private void handleApplicantType(Lead lead, Contact contact, LeadContactPersonType type) {
        if (type == LeadContactPersonType.APPLICANT) {
            // Remove existing applicant
            if (lead.getApplicant() != null) {
                // Delete old applicant from applicant table
                Applicant oldApplicant = applicantRepositoryWrapper.findByIdWithException(lead.getApplicant());
                applicantRepositoryWrapper.delete(oldApplicant);
            }

            // Create new applicant
            Applicant newApplicant = new Applicant();
            newApplicant.setIdentifier(UUID.randomUUID());
            newApplicant.setPersonId(contact.getPersonId());
            Applicant savedApplicant = applicantRepositoryWrapper.saveWithException(newApplicant);

            // Update lead
            lead.setApplicant(savedApplicant.getId());

        } else if (type == LeadContactPersonType.CO_APPLICANT) {
            // Create co-applicant
            Applicant coApplicant = new Applicant();
            coApplicant.setIdentifier(UUID.randomUUID());
            coApplicant.setPersonId(contact.getPersonId());
            Applicant savedCoApplicant = applicantRepositoryWrapper.saveWithException(coApplicant);

            // Add to co-applicants list
            List<Long> coApplicants = lead.getCoApplicants();
            if (coApplicants == null) {
                coApplicants = new ArrayList<>();
            }
            coApplicants.add(savedCoApplicant.getId());
            lead.setCoApplicants(coApplicants);
        }
    }

    private void removeApplicantType(Lead lead, Contact contact, LeadContactPersonType type) {
        if (type == LeadContactPersonType.APPLICANT && lead.getApplicant() != null) {
            // Delete from applicant table
            Applicant applicant = applicantRepositoryWrapper.findByIdWithException(lead.getApplicant());
            applicantRepositoryWrapper.delete(applicant);
            lead.setApplicant(null);

        } else if (type == LeadContactPersonType.CO_APPLICANT && lead.getCoApplicants() != null) {
            // Find and remove co-applicant
            List<Long> coApplicants = lead.getCoApplicants();
            for (Long coApplicantId : new ArrayList<>(coApplicants)) {
                Applicant coApplicant = applicantRepositoryWrapper.findByIdWithException(coApplicantId);
                if (coApplicant.getPersonId().equals(contact.getPersonId())) {
                    applicantRepositoryWrapper.delete(coApplicant);
                    coApplicants.remove(coApplicantId);
                    break;
                }
            }
            lead.setCoApplicants(coApplicants);
        }
    }

    private LeadContactPersonType determineCurrentApplicantType(Lead lead, Contact contact) {
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

    private void unsetOtherDecisionMakers(Lead lead, Long currentContactId) {
        if (lead.getContacts() == null) {
            return;
        }

        for (Long contactId : lead.getContacts()) {
            if (!contactId.equals(currentContactId)) {
                Contact contact = contactRepositoryWrapper.findByIdWithException(contactId);
                if (contact.getIsDecisionMaker()) {
                    contact.setIsDecisionMaker(false);
                    contactRepositoryWrapper.saveWithException(contact);
                }
            }
        }
    }

    private void addContactToLead(Lead lead, Long contactId) {
        List<Long> contacts = lead.getContacts();
        if (contacts == null) {
            contacts = new ArrayList<>();
        }
        contacts.add(contactId);
        lead.setContacts(contacts);
    }

    private void removeContactFromLead(Lead lead, Long contactId) {
        List<Long> contacts = lead.getContacts();
        if (contacts != null) {
            contacts.remove(contactId);
            lead.setContacts(contacts);
        }
    }

    private Contact findContactByIdentifier(Lead lead, UUID contactIdentifier) {
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        if(lead.getContacts() == null || !lead.getContacts().contains(contact.getId())) {
            throw new RuntimeException("Contact not found with identifier: " + contactIdentifier);
        }
        return contact;
    }

    private PersonCreateRequest mapToPersonCreateRequest(LeadContactPersonDetails details) {
        return new PersonCreateRequest(
                details.getFirstName(),
                details.getMiddleName(),
                details.getLastName(),
                details.getMobileNumbers(),
                details.getDateOfBirth(),
                details.getGender()
        );
    }

    private PersonUpdateRequest mapToPersonUpdateRequest(LeadContactPersonDetails details) {
        return new PersonUpdateRequest(
                details.getFirstName(),
                details.getMiddleName(),
                details.getLastName(),
                details.getMobileNumbers(),
                details.getDateOfBirth(),
                details.getGender()
        );
    }

    private void updatePrimaryContactId(Lead lead) {
        if (lead.getContacts() == null || lead.getContacts().isEmpty()) {
            // No contacts, clear primary contact ID
            if (lead.getOtherDetails() != null) {
                lead.getOtherDetails().setPrimaryContactId(null);
            }
            return;
        }

        // Initialize OtherDetails if null
        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = Lead.OtherDetails.builder().build();
            lead.setOtherDetails(otherDetails);
        }

        // First, try to find decision maker
        Long decisionMakerContactId = null;
        for (Long contactId : lead.getContacts()) {
            Contact contact = contactRepositoryWrapper.findByIdWithException(contactId);
            if (Boolean.TRUE.equals(contact.getIsDecisionMaker())) {
                decisionMakerContactId = contactId;
                break;
            }
        }

        // If decision maker exists, use it; otherwise use first contact
        if (decisionMakerContactId != null) {
            otherDetails.setPrimaryContactId(decisionMakerContactId);
        } else {
            // Pick first contact from the list
            otherDetails.setPrimaryContactId(lead.getContacts().getLast());
        }
    }

    private void publishLeadContactCreatedEvent(Lead lead, Contact contact, LeadContactPersonType contactType) {
        LeadContactCreationEventPayload payload = LeadContactCreationEventPayload.builder()
                .leadId(lead.getId())
                .contactId(contact.getId())
                .contactIdentifier(contact.getIdentifier())
                .contactType(contactType != null ? contactType.name() : null)
                .isDecisionMaker(contact.getIsDecisionMaker())
                .isPropertyOwner(contact.getIsPropertyOwner())
                .build();

        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_CONTACT_CREATED.toString(), payload)
        );
    }

    private void publishLeadContactUpdatedEvent(Lead lead, Contact contact, LeadContactPersonType contactType) {
        LeadContactUpdationEventPayload payload = LeadContactUpdationEventPayload.builder()
                .leadId(lead.getId())
                .contactId(contact.getId())
                .contactIdentifier(contact.getIdentifier())
                .contactType(contactType != null ? contactType.name() : null)
                .isDecisionMaker(contact.getIsDecisionMaker())
                .isPropertyOwner(contact.getIsPropertyOwner())
                .build();

        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_CONTACT_UPDATED.toString(), payload)
        );
    }

    private void publishLeadContactDeletedEvent(Lead lead, Contact contact, LeadContactPersonType contactType) {
        LeadContactDeletionEventPayload payload = LeadContactDeletionEventPayload.builder()
                .leadId(lead.getId())
                .contactId(contact.getId())
                .contactIdentifier(contact.getIdentifier())
                .contactType(contactType != null ? contactType.name() : null)
                .isDecisionMaker(contact.getIsDecisionMaker())
                .isPropertyOwner(contact.getIsPropertyOwner())
                .build();

        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_CONTACT_DELETED.toString(), payload)
        );
    }

    @Override
    public String addAddress(UUID contactIdentifier, AddressRequest request) {
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        return personWriteService.addAddress(contact.getPersonId(), request);
    }

    @Override
    public void updateAddress(UUID contactIdentifier, String addressId, AddressRequest request) {
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        personWriteService.updateAddress(contact.getPersonId(), addressId, request);
    }

    @Override
    public IdentifierData addIdentifier(UUID leadId, UUID contactIdentifier, IdentifierRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactIdentifier);
        return personWriteService.addIdentifier(contact.getPersonId(), request);
    }

    @Override
    public void updateIdentifier(UUID leadId, UUID contactIdentifier, UUID identifierId, IdentifierRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactIdentifier);
        personWriteService.updateIdentifier(contact.getPersonId(), identifierId, request);
    }

    @Override
    public void deleteIdentifier(UUID leadId, UUID contactIdentifier, UUID identifierId) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        Contact contact = findContactByIdentifier(lead, contactIdentifier);
        personWriteService.deleteIdentifier(contact.getPersonId(), identifierId);
    }

    @Override
    @Transactional
    public BulkContactsUpdateResponse bulkUpdateContacts(UUID leadId, BulkContactsUpdateRequest request) {
        BulkContactsUpdateResponse.BulkContactsUpdateResponseBuilder responseBuilder = BulkContactsUpdateResponse.builder();
        
        List<LeadContactResponse> createdContacts = new ArrayList<>();
        List<LeadContactResponse> updatedContacts = new ArrayList<>();
        
        int totalProcessed = 0;
        int deletedCount = 0;
        
        // Process creates - if any fails, transaction will rollback
        if (request.getCreates() != null) {
            for (CreateLeadContactRequest createRequest : request.getCreates()) {
                totalProcessed++;
                // We need to get the contact identifier after creation
                // Since createContact doesn't return it, we'll create a modified version
                Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
                
                // Create Person
                PersonCreateRequest personRequest = mapToPersonCreateRequest(createRequest.getContactPersonDetails());
                PersonCreateResponse personResponse = personWriteService.createPerson(personRequest);
                
                // Create Contact
                Contact contact = new Contact();
                contact.setPersonId(personResponse.getId());
                contact.setIsDecisionMaker(createRequest.getIsDecisionMaker());
                contact.setIsPropertyOwner(createRequest.getIsPropertyOwner());
                Contact savedContact = contactRepositoryWrapper.saveWithException(contact);
                UUID createdContactIdentifier = savedContact.getIdentifier();
                
                // Handle decision maker logic
                if (createRequest.getIsDecisionMaker()) {
                    unsetOtherDecisionMakers(lead, savedContact.getId());
                }
                
                // Add contact to lead
                addContactToLead(lead, savedContact.getId());
                
                // Handle applicant type
                handleApplicantType(lead, savedContact, createRequest.getApplicantType());
                
                // Update primary contact ID
                updatePrimaryContactId(lead);
                
                leadRepositoryWrapper.saveWithException(lead);
                
                // Publish event
                publishLeadContactCreatedEvent(lead, savedContact, createRequest.getApplicantType());
                
                // Fetch the created contact
                LeadContactResponse createdContact = leadContactReadService.getContactById(leadId, createdContactIdentifier);
                createdContacts.add(createdContact);
            }
        }
        
        // Process updates - if any fails, transaction will rollback
        if (request.getUpdates() != null) {
            for (BulkContactsUpdateRequest.ContactUpdateItem updateItem : request.getUpdates()) {
                totalProcessed++;
                UUID contactIdentifier = UUID.fromString(updateItem.getContactIdentifier());
                updateContact(leadId, contactIdentifier, updateItem.getData());
                // Fetch the updated contact
                LeadContactResponse updatedContact = leadContactReadService.getContactById(leadId, contactIdentifier);
                updatedContacts.add(updatedContact);
            }
        }
        
        // Process deletes - if any fails, transaction will rollback
        if (request.getDeletes() != null) {
            for (String contactIdentifierStr : request.getDeletes()) {
                totalProcessed++;
                UUID contactIdentifier = UUID.fromString(contactIdentifierStr);
                deleteContact(leadId, contactIdentifier);
                deletedCount++;
            }
        }
        
        // Only return response if all operations succeeded (no exceptions thrown)
        return responseBuilder
                .totalProcessed(totalProcessed)
                .createdContacts(createdContacts)
                .updatedContacts(updatedContacts)
                .deletedCount(deletedCount)
                .build();
    }
}

