package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadService;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class WhatsAppLeadServiceImpl implements WhatsAppLeadService {

    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;
    private final LeadContactReadService leadContactReadService;

    @Override
    @Transactional
    public WhatsAppLeadResponse createOrGetLead(CreateLeadRequest request) {
        // Check if person exists with this phone number
        Optional<Person> existingPerson = personRepositoryWrapper
                .findByPrimaryMobileNumber(request.getPhoneNumber().getMobileNumber());

        if (existingPerson.isPresent()) {
            // Person exists, check if they are a contact in any active lead
            Optional<Lead> existingActiveLead = leadRepositoryWrapper.findActiveLeadByContactPersonId(
                    existingPerson.get().getId()
            );

            if (existingActiveLead.isPresent()) {
                // Lead exists, return existing lead details with full information
                Lead lead = existingActiveLead.get();
                UUID leadIdentifier = lead.getLeadIdentifier();
                UUID contactIdentifier = getPrimaryContactIdentifier(lead);
                
                // Get address from contact
                List<AddressData> addresses = 
                        contactIdentifier != null 
                                ? leadContactReadService.getAddresses(contactIdentifier)
                                : List.of();
                
                // Get preliminary details
                PreliminaryDetailsResponse preliminaryDetails = 
                        leadReadService.getPreliminaryDetails(leadIdentifier);
                
                // Get status
                LeadStatus status = lead.getStatus();
                
                // Get stage from workflow details
                String stage = null;
                if (lead.getWorkflowDetails() != null 
                        && lead.getWorkflowDetails().getCurrentStageDetails() != null) {
                    stage = lead.getWorkflowDetails().getCurrentStageDetails().getStageKey();
                }
                
                return WhatsAppLeadResponse.builder()
                        .leadIdentifier(leadIdentifier)
                        .contactIdentifier(contactIdentifier)
                        .address(addresses)
                        .preliminaryDetails(preliminaryDetails)
                        .status(status)
                        .stage(stage)
                        .build();
            }
        }

        // Lead doesn't exist, delegate to features/lead service to create it
        CreateLeadResponse createResponse = leadWriteService.createLead(request);
        
        // Return simple response for newly created lead
        return WhatsAppLeadResponse.builder()
                .leadIdentifier(createResponse.getLeadIdentifier())
                .contactIdentifier(createResponse.getContactIdentifier())
                .build();
    }

    private UUID getPrimaryContactIdentifier(Lead lead) {
        // Try to get primary contact ID from otherDetails
        Long primaryContactId = null;
        if (lead.getOtherDetails() != null) {
            primaryContactId = lead.getOtherDetails().getPrimaryContactId();
        }

        // If no primary contact ID, use first contact from contacts list
        if (primaryContactId == null && lead.getContacts() != null && !lead.getContacts().isEmpty()) {
            primaryContactId = lead.getContacts().getFirst();
        }

        if (primaryContactId != null) {
            Contact contact = contactRepositoryWrapper.findByIdWithException(primaryContactId);
            return contact.getIdentifier();
        }

        // If no contact found, return null (shouldn't happen for active leads)
        return null;
    }
}
