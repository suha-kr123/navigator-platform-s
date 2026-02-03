package com.nivasafinance.externals.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.externals.lead.dto.CreateLeadRequest;
import com.nivasafinance.externals.lead.dto.CreateLeadResponse;
import com.nivasafinance.externals.lead.service.LeadCreationService;
import com.nivasafinance.features.lead.dto.CreateLeadContactRequest;
import com.nivasafinance.features.lead.dto.CreateLeadContactResponse;
import com.nivasafinance.features.lead.dto.LeadContactPersonDetails;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.externals.lead.exception.ActiveLeadAlreadyExistsWithDetailsException;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisorlead.entity.AdvisorLeadMapping;
import com.nivasafinance.features.advisorlead.repository.AdvisorLeadMappingRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class LeadCreationServiceImpl implements LeadCreationService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final MessageSource messageSource;
    private final ProductReadService productReadService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LeadContactWriteService contactWriteService;
    private final WorkflowConfigRepositoryWrapper workflowConfigRepositoryWrapper;
    private final LeadStageHistoryWriteService leadStageHistoryWriteService;
    private final AdvisorLeadMappingRepositoryWrapper advisorLeadMappingRepositoryWrapper;
    private final AdvisorReadService advisorReadService;
    private final PersonReadService personReadService;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public CreateLeadResponse createLead(CreateLeadRequest request) {
        //validates product exists
        if (request.getProduct() != null) {
            productReadService.getProductByCode(request.getProduct());
        }

        // Check if rejected lead exists - if so, throw exception with REJECTED status
        checkForRejectedLead(request);

        // Check if active lead already exists with this phone number
        checkForActiveLead(request);

        // Create lead
        Lead lead = new Lead();
        lead.setLeadIdentifier(UUID.randomUUID());
        lead.setRequestedAmount(request.getRequestedLoanAmount());
        lead.setProductCode(request.getProduct());
        lead.setStatus(LeadStatus.ACTIVE);
        if (request.getOfficeKey() != null) {
            lead.setOfficeKey(request.getOfficeKey());
        } else {
            lead.setOfficeKey("HQ"); //always goes to HQ for now
        }

        Lead savedLead = leadRepositoryWrapper.saveWithException(lead);

        CreateLeadContactRequest contactPersonDetails = CreateLeadContactRequest
                .builder()
                .contactPersonDetails(LeadContactPersonDetails
                        .builder()
                        .mobileNumbers(List.of(MobileNumberDetails.builder()
                                .number(request.getPhoneNumber().getMobileNumber())
                                .isWhatsappAvailable(request.getPhoneNumber().isWhatsapp())
                                .isPrimary(true)
                                .build()))
                        .build())
                .build();

        CreateLeadContactResponse contactResponse = contactWriteService.createContact(savedLead.getLeadIdentifier(), contactPersonDetails);

        // Create initial stage synchronously and publish event for async task creation
        // TODO: Replace with dynamic workflow picker once design is complete
        String workflowKey = WorkflowConstants.Workflow.DEFAULT_WORKFLOW_KEY;
        String workflowConfigKey = workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(workflowKey).getWorkflowConfigKey();
        leadStageHistoryWriteService.createInitialStage(savedLead.getLeadIdentifier(), workflowConfigKey);
        handleAdvisorMapping(savedLead.getId(), request.getAdvisorIdentifier());
        // Publish LEAD_CREATED event for other listeners (activities, notifications, etc.) - not used by workflow
        publishLeadCreatedEvent(savedLead, request);

        return CreateLeadResponse
                .builder()
                .leadIdentifier(savedLead.getLeadIdentifier())
                .contactIdentifier(contactResponse.getIdentifier())
                .build();
    }

    private void publishLeadCreatedEvent(Lead lead, CreateLeadRequest request) {
        String mobileNumber = request.getPhoneNumber() != null
                ? request.getPhoneNumber().getMobileNumber()
                : null;

        LeadCreationEventPayload payload = LeadCreationEventPayload.builder()
                .id(lead.getId())
                .leadId(lead.getLeadIdentifier())
                .mobileNumber(mobileNumber)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_CREATED.toString(), payload, username)
        );
    }

    private void checkForRejectedLead(CreateLeadRequest request) {
        // If personReadService.getPersonByPrimaryMobile function and catch exception it will throw
        // silently rolled back exceptions
        // As a workaround directly calling personRepositoryWrapper
        Optional<Person> existingPerson = personRepositoryWrapper
                .findByPrimaryMobileNumber(request.getPhoneNumber().getMobileNumber());

        if (existingPerson.isEmpty()) {
            return;
        }
        
        // Person exists, check if they are a contact in any REJECTED lead
        Optional<Lead> existingRejectedLead = findRejectedLeadByContactPersonId(
                existingPerson.get().getId()
        );

        if (existingRejectedLead.isPresent()) {
            Lead lead = existingRejectedLead.get();
            
            // Get lead details
            UUID leadIdentifier = lead.getLeadIdentifier();
            LeadStatus status = lead.getStatus();
            
            // Get stage from workflow_details
            String stage = null;
            if (lead.getWorkflowDetails() != null 
                    && lead.getWorkflowDetails().getCurrentStageDetails() != null) {
                stage = lead.getWorkflowDetails().getCurrentStageDetails().getStageKey();
            }
            
            // Get preliminary_details
            com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse preliminaryDetails = null;
            if (lead.getPreliminaryDetails() != null) {
                preliminaryDetails = com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse.builder()
                        .whatsAppFormDetails(lead.getPreliminaryDetails().getWhatsAppDIYForm())
                        .isWhatsAppDIYFormCompleted(lead.getPreliminaryDetails().getIsWhatsAppDIYFormCompleted())
                        .monthlyFamilyIncome(lead.getPreliminaryDetails().getMonthlyFamilyIncome())
                        .build();
            }
            
            // Get address from primary contact person
            java.util.List<com.nivasafinance.common.dto.AddressData> address = null;
            if (lead.getOtherDetails() != null && lead.getOtherDetails().getPrimaryContactId() != null) {
                try {
                    Contact primaryContact = contactRepositoryWrapper.findByIdWithException(
                            lead.getOtherDetails().getPrimaryContactId());
                    address = personReadService.getAddresses(primaryContact.getPersonId());
                } catch (Exception e) {
                    log.warn("Failed to get address for primary contact: {}", e.getMessage());
                }
            }
            
            // Throw exception with REJECTED status - same format as active lead exception
            throw new ActiveLeadAlreadyExistsWithDetailsException(
                    request.getPhoneNumber().getMobileNumber(),
                    leadIdentifier,
                    address,
                    preliminaryDetails,
                    status,
                    stage,
                    messageSource
            );
        }
    }

    private Optional<Lead> findRejectedLeadByContactPersonId(Long personId) {
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
                  AND l.status = 'REJECTED'
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
            log.error("Failed to find rejected lead by contact person", e);
            return Optional.empty();
        }
    }

    private void checkForActiveLead(CreateLeadRequest request) {
        // If personReadService.getPersonByPrimaryMobile function and catch exception it will throw
        // silently rolled back exceptions
        // As a workaround directly calling personRepositoryWrapper
        Optional<Person> existingPerson = personRepositoryWrapper
                .findByPrimaryMobileNumber(request.getPhoneNumber().getMobileNumber());

        if (existingPerson.isEmpty()) {
            return;
        }
        // Person exists, check if they are a contact in any active/onhold lead
        Optional<Lead> existingActiveLead = leadRepositoryWrapper.findActiveLeadByContactPersonId(
                existingPerson.get().getId()
        );

        if (existingActiveLead.isPresent()) {
            Lead lead = existingActiveLead.get();
            
            // Get lead details
            UUID leadIdentifier = lead.getLeadIdentifier();
            LeadStatus status = lead.getStatus();
            
            // Get stage from workflow_details
            String stage = null;
            if (lead.getWorkflowDetails() != null 
                    && lead.getWorkflowDetails().getCurrentStageDetails() != null) {
                stage = lead.getWorkflowDetails().getCurrentStageDetails().getStageKey();
            }
            
            // Get preliminary_details
            com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse preliminaryDetails = null;
            if (lead.getPreliminaryDetails() != null) {
                preliminaryDetails = com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse.builder()
                        .whatsAppFormDetails(lead.getPreliminaryDetails().getWhatsAppDIYForm())
                        .isWhatsAppDIYFormCompleted(lead.getPreliminaryDetails().getIsWhatsAppDIYFormCompleted())
                        .monthlyFamilyIncome(lead.getPreliminaryDetails().getMonthlyFamilyIncome())
                        .build();
            }
            
            // Get address from primary contact person
            java.util.List<com.nivasafinance.common.dto.AddressData> address = null;
            if (lead.getOtherDetails() != null && lead.getOtherDetails().getPrimaryContactId() != null) {
                try {
                    Contact primaryContact = contactRepositoryWrapper.findByIdWithException(
                            lead.getOtherDetails().getPrimaryContactId());
                    address = personReadService.getAddresses(primaryContact.getPersonId());
                } catch (Exception e) {
                    log.warn("Failed to get address for primary contact: {}", e.getMessage());
                }
            }
            
            throw new ActiveLeadAlreadyExistsWithDetailsException(
                    request.getPhoneNumber().getMobileNumber(),
                    leadIdentifier,
                    address,
                    preliminaryDetails,
                    status,
                    stage,
                    messageSource
            );
        }
    }

    private void handleAdvisorMapping(Long leadId, UUID advisorIdentifier) {
        if (advisorIdentifier != null) {

            // Validate advisor exists - this will throw exception if not found
            AdvisorResponse advisor = advisorReadService.getAdvisorByIdentifier(advisorIdentifier);
            Long advisorId = advisor.getId();

            // Find existing mapping for this lead
            Optional<AdvisorLeadMapping> existingMapping = advisorLeadMappingRepositoryWrapper.findByLeadId(leadId);

            AdvisorLeadMapping mapping;
            if (existingMapping.isPresent()) {
                // Update existing mapping
                mapping = existingMapping.get();
                mapping.setAdvisorId(advisorId);
            } else {
                // Create new mapping
                mapping = new AdvisorLeadMapping();
                mapping.setAdvisorId(advisorId);
                mapping.setLeadId(leadId);
            }
            advisorLeadMappingRepositoryWrapper.saveWithException(mapping);
        } else {
            // Remove mapping if advisorId is null
            Optional<AdvisorLeadMapping> existingMapping = advisorLeadMappingRepositoryWrapper.findByLeadId(leadId);
            existingMapping.ifPresent(advisorLeadMappingRepositoryWrapper::deleteWithException);
        }
    }
}
