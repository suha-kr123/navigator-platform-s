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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public WhatsAppLeadResponse createOrGetLead(CreateLeadRequest request) {
        // Check if person exists with this phone number
        Optional<Person> existingPerson = personRepositoryWrapper
                .findByPrimaryMobileNumber(request.getPhoneNumber().getMobileNumber());

        if (existingPerson.isPresent()) {
            // Person exists, check if they are a contact in any lead
            // Priority: 1) Latest ACTIVE lead, 2) Latest lead of any other status
            Optional<Lead> existingLead = findLeadByContactPersonId(
                    existingPerson.get().getId()
            );

            if (existingLead.isPresent()) {
                // Lead exists, return existing lead details with full information
                Lead lead = existingLead.get();
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

    /**
     * Find lead where the given person is a contact.
     * Priority: 1) Latest ACTIVE lead, 2) Latest lead of any other status
     *
     * @param personId The person ID to search for in contacts
     * @return Optional containing the lead if found, empty otherwise
     */
    private Optional<Lead> findLeadByContactPersonId(Long personId) {
        // First, try to find latest ACTIVE lead
        Optional<Lead> activeLead = findActiveLeadByContactPersonId(personId);
        if (activeLead.isPresent()) {
            return activeLead;
        }

        // If no ACTIVE lead, find latest lead of any status
        return findAnyLeadByContactPersonId(personId);
    }

    /**
     * Find latest ACTIVE lead where the given person is a contact.
     *
     * @param personId The person ID to search for in contacts
     * @return Optional containing the ACTIVE lead if found, empty otherwise
     */
    private Optional<Lead> findActiveLeadByContactPersonId(Long personId) {
        try {
            String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint as id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                  AND l.status = 'ACTIVE'
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;

            Long leadId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                personId
            );
            Lead lead = leadId != null ? leadRepositoryWrapper.findByIdWithException(leadId) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find active lead by contact person", e);
        }
    }

    /**
     * Find latest lead (any status) where the given person is a contact.
     *
     * @param personId The person ID to search for in contacts
     * @return Optional containing the lead if found, empty otherwise
     */
    private Optional<Lead> findAnyLeadByContactPersonId(Long personId) {
        try {
            String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint as id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;

            Long leadId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                personId
            );
            Lead lead = leadId != null ? leadRepositoryWrapper.findByIdWithException(leadId) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find lead by contact person", e);
        }
    }
}
