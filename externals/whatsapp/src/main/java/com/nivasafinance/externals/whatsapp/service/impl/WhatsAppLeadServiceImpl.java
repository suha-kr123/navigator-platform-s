package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadService;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Override
    public Optional<UUID> resolveLeadIdentifierByPrimaryMobileNumber(String mobileNumber) {
        Optional<Person> existingPerson = personRepositoryWrapper.findByPrimaryMobileNumber(mobileNumber);
        if (existingPerson.isEmpty()) {
            return Optional.empty();
        }
        return findLeadByContactPersonId(existingPerson.get().getId()).map(Lead::getLeadIdentifier);
    }

    @Override
    @Transactional
    public WhatsAppLeadResponse createOrGetLead(WhatsAppLeadRequest request) {
        // Map WhatsAppLeadRequest to CreateLeadRequest
        // Always set isWhatsapp to true for WhatsApp API leads
        CreateLeadRequest createLeadRequest = new CreateLeadRequest();
        CreateLeadRequest.MobileNumberDetails mobileNumberDetails = 
                new CreateLeadRequest.MobileNumberDetails(
                        request.getMobileNumber(),
                        true  // Always true for WhatsApp API
                );
        createLeadRequest.setPhoneNumber(mobileNumberDetails);

        // Check if person exists with this phone number
        Optional<Person> existingPerson = personRepositoryWrapper
                .findByPrimaryMobileNumber(request.getMobileNumber());

        UUID leadIdentifier;
        UUID contactIdentifier;

        if (existingPerson.isPresent()) {
            // Person exists, check if they are a contact in any lead
            // Priority: 1) Latest ACTIVE lead, 2) Latest lead of any other status
            Optional<Lead> existingLead = findLeadByContactPersonId(
                    existingPerson.get().getId()
            );

            if (existingLead.isPresent()) {
                // Lead exists, return existing lead details with full information
                Lead lead = existingLead.get();
                leadIdentifier = lead.getLeadIdentifier();
                contactIdentifier = getPrimaryContactIdentifier(lead);
                
                // Get address from contact (full address data from address table)
                List<AddressData> addresses = 
                        contactIdentifier != null 
                                ? leadContactReadService.getAddresses(contactIdentifier)
                                : List.of();
                
                // Get preliminary details (JSON string if exists, "empty" if not exists)
                String preliminaryDetails = "empty";
                try {
                    PreliminaryDetailsResponse preliminaryDetailsObj = leadReadService.getPreliminaryDetails(leadIdentifier);
                    if (preliminaryDetailsObj != null) {
                        // Check if preliminary details has any data
                        boolean hasData = (preliminaryDetailsObj.getWhatsAppFormDetails() != null && !preliminaryDetailsObj.getWhatsAppFormDetails().isEmpty())
                                || preliminaryDetailsObj.getIsWhatsAppDIYFormCompleted() != null
                                || preliminaryDetailsObj.getMonthlyFamilyIncome() != null;
                        
                        if (hasData) {
                            // Convert to JSON string
                            preliminaryDetails = objectMapper.writeValueAsString(preliminaryDetailsObj);
                        } else {
                            preliminaryDetails = "empty";
                        }
                    }
                } catch (Exception e) {
                    preliminaryDetails = "empty";
                }
                
                // Get status ("empty" if not exists)
                LeadStatus statusEnum = lead.getStatus();
                String status = (statusEnum != null) ? statusEnum.toString() : "empty";
                
                // Get substatus ("empty" if not exists)
                LeadSubStatus substatusEnum = lead.getSubstatus();
                String substatus = (substatusEnum != null) ? substatusEnum.toString() : "empty";
                
                // Get reasons (JSON string if exists, "empty" if not exists)
                Lead.ReasonDetails reasonsObj = lead.getReasons();
                String reasons = "empty";
                if (reasonsObj != null) {
                    // Check if reasons has any data
                    boolean hasData = (reasonsObj.getReject() != null && !reasonsObj.getReject().isBlank())
                            || (reasonsObj.getWithdrawn() != null && !reasonsObj.getWithdrawn().isBlank())
                            || (reasonsObj.getOnhold() != null && !reasonsObj.getOnhold().isBlank())
                            || (reasonsObj.getDropoff() != null && !reasonsObj.getDropoff().isBlank());
                    
                    if (hasData) {
                        // Convert to JSON string
                        try {
                            reasons = objectMapper.writeValueAsString(reasonsObj);
                        } catch (Exception e) {
                            reasons = "empty";
                        }
                    }
                }
                
                // Get stage from workflow details ("empty" if not exists)
                String stage = null;
                if (lead.getWorkflowDetails() != null 
                        && lead.getWorkflowDetails().getCurrentStageDetails() != null) {
                    stage = lead.getWorkflowDetails().getCurrentStageDetails().getStageKey();
                }
                if (stage == null || stage.isBlank()) {
                    stage = "empty";
                }
                
                // Get name from person ("empty" if not exists)
                String name = "empty";
                if (contactIdentifier != null) {
                    Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
                    Person person = personRepositoryWrapper.findByIdWithException(contact.getPersonId());
                    String displayName = person.getDisplayName();
                    if (displayName != null && !displayName.isBlank()) {
                        name = displayName;
                    }
                }

                String[] productFields = resolveProductFields(lead, leadIdentifier);

                return WhatsAppLeadResponse.builder()
                        .leadIdentifier(leadIdentifier)
                        .contactIdentifier(contactIdentifier)
                        .address(addresses != null ? addresses : List.of())
                        .preliminaryDetails(preliminaryDetails)
                        .status(status)
                        .substatus(substatus)
                        .reasons(reasons)
                        .stage(stage)
                        .name(name)
                        .productCode(productFields[0])
                        .productName(productFields[1])
                        .build();
            }
        }

        // Lead doesn't exist, delegate to features/lead service to create it
        CreateLeadResponse createResponse = leadWriteService.createLead(createLeadRequest);
        leadIdentifier = createResponse.getLeadIdentifier();
        contactIdentifier = createResponse.getContactIdentifier();
        
        // Update sourcing details if provided (only for new lead creation)
        if (request.getSourcing_channel_name() != null) {
            updateSourcingDetails(leadIdentifier, request);
        }

        String[] productFields = resolveProductFields(null, leadIdentifier);

        return WhatsAppLeadResponse.builder()
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .productCode(productFields[0])
                .productName(productFields[1])
                .build();
    }

    private String[] resolveProductFields(Lead leadOrNull, UUID leadIdentifier) {
        Lead lead = leadOrNull;
        if (lead == null) {
            try {
                lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
            } catch (Exception e) {
                log.warn("Failed to fetch lead for product fields, leadIdentifier: {}", leadIdentifier, e);
                return new String[] {"empty", "empty"};
            }
        }
        if (lead == null) {
            return new String[] {"empty", "empty"};
        }
        String rawCode = lead.getProductCode();
        if (rawCode == null || rawCode.isBlank()) {
            return new String[] {"empty", "empty"};
        }

        String productCode = rawCode.trim();
        String productName = "empty";
        try {
            LeadResponse lr = leadReadService.getLeadByIdentifier(leadIdentifier);
            if (lr != null) {
                if (lr.getProductCode() != null && !lr.getProductCode().isBlank()) {
                    productCode = lr.getProductCode();
                }
                if (lr.getProductName() != null && !lr.getProductName().isBlank()) {
                    productName = lr.getProductName();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to enrich product fields from lead read service, leadIdentifier: {}", leadIdentifier, e);
            productName = "empty";
        }
        return new String[] {productCode, productName};
    }

    private void updateSourcingDetails(UUID leadIdentifier, WhatsAppLeadRequest request) {
        UpdateSourcingDetailsRequest sourcingRequest = new UpdateSourcingDetailsRequest();
        sourcingRequest.setSourcingChannel(request.getSourcing_channel_name());
        
        if (request.getMarketing_details() != null) {
            boolean hasSourceId = request.getMarketing_details().getSourceId() != null && 
                                  !request.getMarketing_details().getSourceId().isBlank();
            boolean hasSourceUrl = request.getMarketing_details().getSourceUrl() != null && 
                                  !request.getMarketing_details().getSourceUrl().isBlank();
            
            // If at least one has a value, update both fields (even if one is empty)
            if (hasSourceId || hasSourceUrl) {
                sourcingRequest.setSourceId(request.getMarketing_details().getSourceId());
                sourcingRequest.setSourceUrl(request.getMarketing_details().getSourceUrl());
            }
            // If both are empty/null, don't update marketing_details (only sourcing_channel_name will be updated)
        }
        
        leadWriteService.updateSourcingDetails(leadIdentifier, sourcingRequest);
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
