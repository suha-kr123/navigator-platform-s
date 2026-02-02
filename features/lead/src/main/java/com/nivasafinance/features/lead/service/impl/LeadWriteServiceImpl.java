package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadPreliminaryDetailsUpdateEventPayload;
import com.nivasafinance.common.events.payload.LeadStatusChangeEventPayload;
import com.nivasafinance.common.events.payload.LeadUpdateEventPayload;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisorlead.entity.AdvisorLeadMapping;
import com.nivasafinance.features.advisorlead.repository.AdvisorLeadMappingRepositoryWrapper;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.dto.UpdateCallDetailsRequest;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.ActiveLeadAlreadyExistsException;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import com.nivasafinance.features.lead.annotation.TransactionalOptimisticRetry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes.LEAD_PRIORITY_MASTER;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class LeadWriteServiceImpl implements LeadWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final MessageSource messageSource;
    private final AddressDataService addressDataService;
    private final SourcingChannelWriteService sourcingChannelWriteService;
    private final ProductReadService productReadService;
    private final CodeValueMasterService codeValueMasterService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LeadContactWriteService contactWriteService;
    private final WorkflowConfigRepositoryWrapper workflowConfigRepositoryWrapper;
    private final LeadStageHistoryWriteService leadStageHistoryWriteService;
    private final AdvisorLeadMappingRepositoryWrapper advisorLeadMappingRepositoryWrapper;
    private final AdvisorReadService advisorReadService;


    @Override
    @Transactional
    public CreateLeadResponse createLead(CreateLeadRequest request) {
        //validates product exists
        if (request.getProduct() != null) {
            productReadService.getProductByCode(request.getProduct());
        }

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

    @Override
    @TransactionalOptimisticRetry
    public void updateLead(UUID leadIdentifier, UpdateLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        lead.setRequestedAmount(request.getRequestedAmount());
        if (request.getOfficeKey() == null) {
            throw new BadRequestException("Office key is required and cannot be null");
        }
        lead.setOfficeKey(request.getOfficeKey());
        lead.setOwner(request.getOwner());
        if (request.getPurpose() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getPurpose(), SystemControlledMasterCodes.LEAD_PURPOSE_MASTER);
            lead.setPurpose(request.getPurpose());
        } else {
            lead.setPurpose(null);
        }
        //validates product exists
        if (request.getProductCode() != null) {
            productReadService.getProductByCode(request.getProductCode());
            lead.setProductCode(request.getProductCode());
        }

        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new Lead.OtherDetails();
        }
        if (request.getPreferredCallStartTime() != null && request.getPreferredCallEndTime() != null) {
            if (request.getPreferredCallStartTime().isAfter(request.getPreferredCallEndTime())) {
                throw new BadRequestException("Preferred call start time cannot be after preferred call end time");
            }
            otherDetails.setPreferredCallStartTime(request.getPreferredCallStartTime());
            otherDetails.setPreferredCallEndTime(request.getPreferredCallEndTime());
        } else {
            otherDetails.setPreferredCallStartTime(null);
            otherDetails.setPreferredCallEndTime(null);
        }
        if (request.getPriority() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getPriority(), LEAD_PRIORITY_MASTER);
            otherDetails.setPriority(request.getPriority());
        }
        lead.setOtherDetails(otherDetails);

        // Handle advisor mapping
        handleAdvisorMapping(lead.getId(), request.getAdvisorId());

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
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

    @Override
    public void touchLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @TransactionalOptimisticRetry
    public void updatePreliminaryDetails(UUID leadIdentifier, UpdatePreliminaryDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.PreliminaryDetails preliminaryDetails = lead.getPreliminaryDetails();
        if (preliminaryDetails == null) {
            preliminaryDetails = new Lead.PreliminaryDetails();
        }
        if(request.getWhatsAppFormDetails() != null) {
            preliminaryDetails.setWhatsAppDIYForm(request.getWhatsAppFormDetails());
        }
        preliminaryDetails.setIsWhatsAppDIYFormCompleted(request.getIsWhatsAppDIYFormCompleted());
        preliminaryDetails.setMonthlyFamilyIncome(request.getMonthlyFamilyIncome());
        lead.setPreliminaryDetails(preliminaryDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadPreliminaryDetailsUpdatedEvent(lead, preliminaryDetails);
    }

    @Override
    @Transactional
    public void updateCreditDetails(UUID leadIdentifier, UpdateCreditDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Get current username from UserContext
        String currentUsername = UserContext.getUsername();

        Lead.CreditRatingDetails creditDetails = lead.getCreditRatingDetails();
        if (creditDetails == null) {
            creditDetails = new Lead.CreditRatingDetails();
        }

        // Validate and set code values
        if (request.getOccupationProfile() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getOccupationProfile(), SystemControlledMasterCodes.LEAD_OCCUPATION_PROFILE_MASTER);
            creditDetails.setOccupationProfile(request.getOccupationProfile());
        }
        if (request.getRoofProfile() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getRoofProfile(), SystemControlledMasterCodes.LEAD_ROOF_PROFILE_MASTER);
            creditDetails.setRoofProfile(request.getRoofProfile());
        }
        if (request.getLtv() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getLtv(), SystemControlledMasterCodes.LEAD_LTV_MASTER);
            creditDetails.setLtv(request.getLtv());
        }
        if (request.getFoir() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getFoir(), SystemControlledMasterCodes.LEAD_FOIR_MASTER);
            creditDetails.setFoir(request.getFoir());
        }
        if (request.getMonthlyFamilyIncome() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getMonthlyFamilyIncome(), SystemControlledMasterCodes.LEAD_MONTHLY_INCOME_MASTER);
            creditDetails.setMonthlyFamilyIncome(request.getMonthlyFamilyIncome());
        }
        if (request.getPropertyDocumentType() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getPropertyDocumentType(), SystemControlledMasterCodes.LEAD_PROPERTY_DOCUMENT_TYPE_MASTER);
            creditDetails.setPropertyDocumentType(request.getPropertyDocumentType());
        }
        creditDetails.setEligibleLoanAmount(request.getEligibleLoanAmount());
        if (request.getLocation() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getLocation(), SystemControlledMasterCodes.LEAD_LOCATION_MASTER);
            creditDetails.setLocation(request.getLocation());
        }
        if (request.getBureauRating() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getBureauRating(), SystemControlledMasterCodes.LEAD_BUREAU_RATING_MASTER);
            creditDetails.setBureauRating(request.getBureauRating());
        }
        if (request.getCustomerProfiles() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getCustomerProfiles(), SystemControlledMasterCodes.LEAD_CUSTOMER_PROFILE_MASTER);
            creditDetails.setCustomerProfiles(request.getCustomerProfiles());
        }
        // Set underwriter only if it's not already set (first time creation)
        if (creditDetails.getUnderwriter() == null || creditDetails.getUnderwriter().trim().isEmpty()) {
            creditDetails.setUnderwriter(currentUsername);
        }

        lead.setCreditRatingDetails(creditDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void updateProposedDetails(UUID leadIdentifier, UpdateProposedDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        Lead.ProposedDetails proposedDetails = lead.getProposedDetails();
        if (proposedDetails == null) {
            proposedDetails = new Lead.ProposedDetails();
        }

        proposedDetails.setProposedLoanAmount(request.getProposedLoanAmount());
        proposedDetails.setRoi(request.getRoi());
        proposedDetails.setTenureValue(request.getTenureValue());
        proposedDetails.setTenureType(request.getTenureType());
        proposedDetails.setEmi(request.getEmi());

        lead.setProposedDetails(proposedDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void updatePropertyDetails(UUID leadIdentifier, UpdatePropertyDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new Lead.OtherDetails();
        }

        Lead.PropertyDetails propertyDetails = otherDetails.getPropertyDetails();
        if (propertyDetails == null) {
            propertyDetails = new Lead.PropertyDetails();
        }

        // Use common address data service for address creation
        AddressData addressData = addressDataService.createAddressData(request.getAddress());

        propertyDetails.setAddress(addressData);

        otherDetails.setPropertyDetails(propertyDetails);
        lead.setOtherDetails(otherDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    private PersonCreateRequest getPersonCreateRequest(CreateLeadRequest request) {
        List<MobileNumberDetails> mobileNumbers = getMobileNumberDetails(request);
        return new PersonCreateRequest(
                null, // firstName
                null, // middleName
                null, // lastName
                mobileNumbers,
                null, // dateOfBirth
                null // gender
        );
    }

    private List<MobileNumberDetails> getMobileNumberDetails(CreateLeadRequest request) {
        // Create person with phoneNo
        MobileNumberDetails mobileNumber = new MobileNumberDetails();
        mobileNumber.setNumber(request.getPhoneNumber().getMobileNumber());
        mobileNumber.setIsPrimary(true);
        mobileNumber.setIsWhatsappAvailable(request.getPhoneNumber().isWhatsapp());

        List<MobileNumberDetails> mobileNumbers = new ArrayList<>();
        mobileNumbers.add(mobileNumber);
        return mobileNumbers;
    }

    @Override
    @Transactional
    public void updateSourcingDetails(UUID leadIdentifier, UpdateSourcingDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        SourcingChannelRequest sourcingChannelRequest = new SourcingChannelRequest(
                request.getSourcingChannel(),
                request.getMarketingSource(),
                SourcingChannelRequest.MarketingDetails.builder()
                        .sourceId(request.getSourceId())
                        .sourceUrl(request.getSourceUrl())
                        .campaignId(request.getCampaignId())
                        .sourcedBy(request.getSourcedBy())
                        .build()
        );

        if (lead.getSourcingChannelId() != null) {
            sourcingChannelWriteService.update(lead.getSourcingChannelId(), sourcingChannelRequest);
        } else {
            SourcingChannelResponse sourcingChannelResponse =
                sourcingChannelWriteService.create(sourcingChannelRequest);
            lead.setSourcingChannelId(sourcingChannelResponse.getId());
        }

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void updateCallDetails(UUID leadIdentifier, UpdateCallDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new Lead.OtherDetails();
        }

        otherDetails.setNoOfCampaignCalls(request.getNoOfCampaignCalls());
        lead.setOtherDetails(otherDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void updateDisbursementDetails(UUID leadIdentifier, UpdateDisbursementDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Get existing disbursement details or create new instance
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null) {
            disbursementDetails = new Lead.DisbursementDetails();
        }

        // Preserve existing tranches
        List<Lead.Tranche> existingTranches = disbursementDetails.getTranches();

        // Set all disbursement fields from request (PUT semantics - full replacement)
        disbursementDetails.setDisbursedAmount(request.getDisbursedAmount());
        disbursementDetails.setRoi(request.getRoi());
        disbursementDetails.setTenureValue(request.getTenureValue());
        disbursementDetails.setTenureType(request.getTenureType());
        disbursementDetails.setDisbursedDate(request.getDisbursedDate());
        disbursementDetails.setProcessingFees(request.getProcessingFees());

        // Restore tranches (preserve existing tranches)
        disbursementDetails.setTranches(existingTranches);

        lead.setDisbursementDetails(disbursementDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void createTranche(UUID leadIdentifier, CreateTrancheRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate that disbursement details exist
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null) {
            throw new BadRequestException("Disbursement details must exist before adding tranches");
        }

        // Get existing tranches or create new list
        List<Lead.Tranche> tranches = disbursementDetails.getTranches();
        if (tranches == null) {
            tranches = new ArrayList<>();
        }

        // Create new tranche
        Lead.Tranche tranche = Lead.Tranche.builder()
                .identifier(UUID.randomUUID())
                .amount(request.getAmount())
                .date(request.getDate())
                .build();

        tranches.add(tranche);
        disbursementDetails.setTranches(tranches);
        lead.setDisbursementDetails(disbursementDetails);
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void updateTranche(UUID leadIdentifier, UUID trancheIdentifier, UpdateTrancheRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate that disbursement details exist
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null || disbursementDetails.getTranches() == null) {
            throw new ResourceNotFoundException("Tranche not found with identifier: " + trancheIdentifier);
        }

        // Find tranche by identifier
        Lead.Tranche tranche = disbursementDetails.getTranches().stream()
                .filter(t -> trancheIdentifier.equals(t.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Tranche not found with identifier: " + trancheIdentifier));

        // Update tranche fields (PUT semantics - full replacement)
        tranche.setAmount(request.getAmount());
        tranche.setDate(request.getDate());

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void deleteTranche(UUID leadIdentifier, UUID trancheIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate that disbursement details exist
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null || disbursementDetails.getTranches() == null) {
            throw new ResourceNotFoundException("Tranche not found with identifier: " + trancheIdentifier);
        }

        // Find and remove tranche by identifier
        List<Lead.Tranche> tranches = disbursementDetails.getTranches();
        boolean removed = tranches.removeIf(t -> trancheIdentifier.equals(t.getIdentifier()));

        if (!removed) {
            throw new ResourceNotFoundException("Tranche not found with identifier: " + trancheIdentifier);
        }

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadUpdatedEvent(lead);
    }

    @Override
    @Transactional
    public void rejectLead(UUID leadIdentifier, RejectLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Set status to REJECTED
        lead.setStatus(LeadStatus.REJECTED);

        // Clear substatus when rejecting
        lead.setSubstatus(null);

        // Store reason code if provided
        if (request.getReasonCode() != null) {
            //validate reason
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getReasonCode(), SystemControlledMasterCodes.LEAD_REJECT_REASON_MASTER);
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setOnhold(null);
            reasons.setReject(request.getReasonCode());
            lead.setReasons(reasons);
        }

        Lead.RejectionDetails rejectionDetails = lead.getRejectionDetails();
        if(rejectionDetails == null) {
            rejectionDetails = new Lead.RejectionDetails();
        }

        rejectionDetails.setRejectionDate(LocalDateTime.now());
        rejectionDetails.setRejectedBy(UserContext.getUsername());

        lead.setRejectionDetails(rejectionDetails);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event (task closing will be handled asynchronously by TaskCloseListener)
        String reason = lead.getReasons() != null ? lead.getReasons().getReject() : null;
        publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_REJECTED, reason);
    }

    @Override
    @Transactional
    public void undoRejectLead(UUID leadIdentifier) {
    Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

    // Validate that lead is currently rejected
    if (!LeadStatus.REJECTED.equals(lead.getStatus())) {
        throw new BadRequestException("Cannot undo reject, lead is not in REJECTED status. Current status: " + lead.getStatus());
    }

    // Revert status to ACTIVE
    lead.setStatus(LeadStatus.ACTIVE);

    // Ensure substatus is null
    lead.setSubstatus(null);

    // Clear reject reason from reasons.reject
    if (lead.getReasons() != null) {
        Lead.ReasonDetails reasons = lead.getReasons();
        reasons.setReject(null);
        lead.setReasons(reasons);
    }

    // Clear rejection details (rejectionDate and rejectedBy)
    lead.setRejectionDetails(null);

    leadRepositoryWrapper.saveWithException(lead);

    // Publish event
    publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_REJECTION_UNDO, null);
}

    @Override
    @Transactional
    public void withdrawLead(UUID leadIdentifier, WithdrawLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Set status to WITHDRAWN
        lead.setStatus(LeadStatus.WITHDRAWN);

        // Clear substatus when withdrawing
        lead.setSubstatus(null);

        // Store reason code if provided
        if (request.getReasonCode() != null) {
            //validate reason
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getReasonCode(), SystemControlledMasterCodes.LEAD_WITHDRAWAL_REASON_MASTER);
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setOnhold(null);
            reasons.setWithdrawn(request.getReasonCode());
            lead.setReasons(reasons);
        }

        Lead.WithdrawnDetails withdrawnDetails = lead.getWithdrawnDetails();
        if(withdrawnDetails == null) {
            withdrawnDetails = new Lead.WithdrawnDetails();
        }

        withdrawnDetails.setWithdrawnDate(LocalDateTime.now());
        withdrawnDetails.setWithdrawnBy(UserContext.getUsername());

        lead.setWithdrawnDetails(withdrawnDetails);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event (task closing will be handled asynchronously by TaskCloseListener)
        String reason = lead.getReasons() != null ? lead.getReasons().getWithdrawn() : null;
        publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_WITHDRAWN, reason);
    }

    @Override
    @Transactional
    public void completeLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate lead is not on hold
        if (LeadSubStatus.ONHOLD.equals(lead.getSubstatus())) {
            throw new BadRequestException("Cannot complete lead, lead is on hold");
        }

        // Set status to COMPLETED
        lead.setStatus(LeadStatus.COMPLETED);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event (task closing will be handled asynchronously by TaskCloseListener)
        publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_COMPLETED, null);
    }

    @Override
    @Transactional
    public void onholdLead(UUID leadIdentifier, OnholdLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate lead is not rejected or withdrawn
        if (LeadStatus.REJECTED.equals(lead.getStatus()) || LeadStatus.WITHDRAWN.equals(lead.getStatus())) {
            throw new BadRequestException("Cannot put lead on hold, lead is already rejected or withdrawn");
        }

        // Check if lead is already on hold (before we set it)
        boolean isAlreadyOnHold = LeadSubStatus.ONHOLD.equals(lead.getSubstatus());

        // Set substatus to ONHOLD (keep current status)
        lead.setSubstatus(LeadSubStatus.ONHOLD);

        // Store reason code if provided
        if (request.getReasonCode() != null) {
            //validate reason
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getReasonCode(), SystemControlledMasterCodes.LEAD_ONHOLD_REASON_MASTER);
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setOnhold(request.getReasonCode());
            lead.setReasons(reasons);
        }

        // Set onHoldDetails
        Lead.OnHoldDetails onHoldDetails = lead.getOnHoldDetails();
        if (onHoldDetails == null) {
            onHoldDetails = new Lead.OnHoldDetails();
        }
        // Only update movement date and who put it on hold if not already on hold (first time putting on hold)
        if (!isAlreadyOnHold) {
            onHoldDetails.setOnHoldMovementDate(LocalDateTime.now());
            onHoldDetails.setOnHoldBy(UserContext.getUsername());
        }
        // Always update follow-up date (allows updating it when already on hold)
        onHoldDetails.setHoldFollowUpDate(request.getHoldFollowUpDate());
        lead.setOnHoldDetails(onHoldDetails);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        String reason = lead.getReasons() != null ? lead.getReasons().getOnhold() : null;
        publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_ON_HOLD, reason);
    }

    @Override
    @Transactional
    public void resumeLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Clear substatus to resume lead
        lead.setSubstatus(null);

        // Clear onhold reason if exists
        if (lead.getReasons() != null) {
            Lead.ReasonDetails reasons = lead.getReasons();
            reasons.setOnhold(null);
            lead.setReasons(reasons);
        }

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_RESUMED, null);
    }

    @Override
    @Transactional
    public void dropoffLead(UUID leadIdentifier, DropoffLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate lead is not rejected or withdrawn
        if (!LeadStatus.ACTIVE.equals(lead.getStatus())) {
            throw new BadRequestException("Only Active Lead can be put on dropoff");
        }

        // Set substatus to DROPOFF (keep current status)
        lead.setSubstatus(LeadSubStatus.DROPOFF);

        // Store reason code if provided
        if (request.getReasonCode() != null) {
            //validate reason
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getReasonCode(), SystemControlledMasterCodes.LEAD_DROPOFF_REASON_MASTER);
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setDropoff(request.getReasonCode());
            lead.setReasons(reasons);
        }

        Lead.DropoffDetails dropoffDetails = lead.getDropoffDetails();
        if (dropoffDetails == null) {
            dropoffDetails = new Lead.DropoffDetails();
        }

        dropoffDetails.setDropoffDate(LocalDateTime.now());
        dropoffDetails.setDropoffBy(UserContext.getUsername());

        lead.setDropoffDetails(dropoffDetails);

        leadRepositoryWrapper.saveWithException(lead);

        // Publish event (task closing will be handled asynchronously by TaskCloseListener)
        String reason = lead.getReasons() != null ? lead.getReasons().getDropoff() : null;
        publishLeadStatusChangeEvent(lead, BusinessEvent.LEAD_DROPOFF, reason);
    }

    private void publishLeadStatusChangeEvent(Lead lead, BusinessEvent event, String reason) {
        LeadStatusChangeEventPayload payload = LeadStatusChangeEventPayload.builder()
                .leadId(lead.getId())
                .leadIdentifier(lead.getLeadIdentifier())
                .reason(reason)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(event.toString(), payload, username)
        );
    }

    private void publishLeadUpdatedEvent(Lead lead) {
        LeadUpdateEventPayload payload = LeadUpdateEventPayload.builder()
                .leadId(lead.getId())
                .leadIdentifier(lead.getLeadIdentifier())
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_UPDATED.toString(), payload, username)
        );
    }

    private void publishLeadPreliminaryDetailsUpdatedEvent(Lead lead, Lead.PreliminaryDetails preliminaryDetails) {
        LeadPreliminaryDetailsUpdateEventPayload payload = LeadPreliminaryDetailsUpdateEventPayload.builder()
                .leadId(lead.getId())
                .leadIdentifier(lead.getLeadIdentifier())
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_PRELIMINARY_DETAILS_UPDATED.toString(), payload, username)
        );
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
            throw new ActiveLeadAlreadyExistsException(
                    request.getPhoneNumber().getMobileNumber(),
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

    @Override
    @Transactional
    public BulkSalesOwnerAssignmentResponse bulkAssignSalesOwner(BulkSalesOwnerAssignmentRequest request) {
        // Input validation
        if (request == null) {
            throw new BadRequestException("Request cannot be null");
        }
        if (request.getSalesOwner() == null || request.getSalesOwner().trim().isEmpty()) {
            throw new BadRequestException("Sales owner is required");
        }
        if (request.getLeadIdentifiers() == null || request.getLeadIdentifiers().isEmpty()) {
            throw new BadRequestException("Lead identifiers are required");
        }
        
        List<UUID> successfulLeadIdentifiers = new ArrayList<>();
        List<BulkSalesOwnerAssignmentResponse.AssignmentError> errors = new ArrayList<>();
        
        for (UUID leadIdentifier : request.getLeadIdentifiers()) {
            try {
                Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
                lead.setOwner(request.getSalesOwner());
                leadRepositoryWrapper.saveWithException(lead);
                successfulLeadIdentifiers.add(leadIdentifier);
                log.debug("Successfully assigned sales owner {} to lead {}", request.getSalesOwner(), leadIdentifier);
            } catch (Exception e) {
                BulkSalesOwnerAssignmentResponse.AssignmentError error = 
                        BulkSalesOwnerAssignmentResponse.AssignmentError.builder()
                                .leadIdentifier(leadIdentifier)
                                .errorMessage(e.getMessage() != null ? e.getMessage() : "Unknown error")
                                .build();
                errors.add(error);
                log.warn("Failed to assign sales owner {} to lead {}: {}", 
                        request.getSalesOwner(), leadIdentifier, e.getMessage(), e);
            }
        }
        
        int totalRequested = request.getLeadIdentifiers().size();
        int successful = successfulLeadIdentifiers.size();
        int failed = errors.size();
        
        return BulkSalesOwnerAssignmentResponse.builder()
                .totalRequested(totalRequested)
                .successful(successful)
                .failed(failed)
                .successfulLeadIdentifiers(successfulLeadIdentifiers)
                .errors(errors)
                .build();
    }
}
