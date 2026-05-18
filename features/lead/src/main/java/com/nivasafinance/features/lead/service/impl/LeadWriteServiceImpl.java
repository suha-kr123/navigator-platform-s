package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.analytics.AnalyticsEvent;
import com.nivasafinance.analytics.AnalyticsHelper;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.PatchAddressData;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadPreliminaryDetailsUpdateEventPayload;
import com.nivasafinance.common.events.payload.LeadStatusChangeEventPayload;
import com.nivasafinance.common.events.payload.LeadUpdateEventPayload;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lead.annotation.TransactionalOptimisticRetry;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.*;
import com.nivasafinance.features.lead.exception.ActiveLeadAlreadyExistsException;
import com.nivasafinance.features.lead.exception.LeadExceptionFactory;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes.LEAD_INTENT_MASTER;
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
    private final AnalyticsHelper analyticsHelper;


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
        handleSourcingChannel(savedLead, request.getSourcingChannelRequest());
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
        if (request.getCustomerConvinceStatus() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getCustomerConvinceStatus(),
                    SystemControlledMasterCodes.LEAD_CUSTOMER_CONVINCE_STATUS_MASTER);
            lead.setCustomerConvinceStatus(request.getCustomerConvinceStatus());
        } else {
            lead.setCustomerConvinceStatus(null);
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
                throw new BadRequestException("lead.validation.preferred_call_time_invalid");
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
        if (request.getIntent() != null) {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getIntent(), LEAD_INTENT_MASTER);
            otherDetails.setIntent(request.getIntent());
        }
        lead.setOtherDetails(otherDetails);

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
                        .referredByCode(request.getReferredByCode())
                        .googleClickId(request.getGoogleClickId())
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
        rejectionDetails.setRemarks(request.getRemarks());

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

    @Override
    public void patchPropertyDetails(UUID leadIdentifier, PatchPropertyDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new Lead.OtherDetails();
        }
        mergePropertyDetailsInto(leadIdentifier, otherDetails, request);
        lead.setOtherDetails(otherDetails);
        leadRepositoryWrapper.saveWithException(lead);
        publishLeadUpdatedEvent(lead);
    }

    @Override
    public void patchIncomeObligationDetails(UUID leadIdentifier, PatchIncomeAndObligationRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        mergeIncomeObligationDetailsInto(lead, request);
        leadRepositoryWrapper.saveWithException(lead);
        if(request.getIncomeDetails().isPresent()) {
            List<String> incomes = new ArrayList<>();
            for (PatchIncomeAndObligationRequest.IncomeDetailsData incomeDetailsData : request.getIncomeDetails().get()) {
                incomes.add(incomeDetailsData.getIncomeSource());
            }
            analyticsHelper.captureLead(new AnalyticsEvent(leadIdentifier.toString(), "income_type_selected", List.of(new AnalyticsEvent.Param("income_type", String.join(",",incomes)))));
        }
        publishLeadUpdatedEvent(lead);
    }

    @Override
    public void patchLead(UUID leadIdentifier, PatchLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new Lead.OtherDetails();
        }
        if (request.getPropertyDetails() != null) {
            mergePropertyDetailsInto(leadIdentifier, otherDetails, request.getPropertyDetails().orElse(null));
        }
        if (request.getIncomeAndObligationDetails() != null) {
            mergeIncomeObligationDetailsInto(lead, request.getIncomeAndObligationDetails().orElse(null));
        }
        if (request.getCurrentCustomerFormStep() != null) {
            mergeCurrentCustomerFormStepInto(otherDetails, request.getCurrentCustomerFormStep().orElse(null));
        }
        if (request.getProductCode() != null) {
            String productCode = request.getProductCode().orElse(null);
            if (productCode != null) {
                productReadService.getProductByCode(productCode);
            }
            lead.setProductCode(productCode);
        }
        if (request.getRequestedAmount() != null) {
            lead.setRequestedAmount(request.getRequestedAmount().orElse(null));
        }
        if (request.getPreferredCallStartTime() != null) {
            otherDetails.setPreferredCallStartTime(request.getPreferredCallStartTime().orElse(null));
        }
        if (request.getPreferredCallEndTime() != null) {
            otherDetails.setPreferredCallEndTime(request.getPreferredCallEndTime().orElse(null));
        }
        if (otherDetails.getPreferredCallStartTime() != null
                && otherDetails.getPreferredCallEndTime() != null
                && otherDetails.getPreferredCallStartTime().isAfter(otherDetails.getPreferredCallEndTime())) {
            throw new BadRequestException("lead.validation.preferred_call_time_invalid");
        }
        lead.setOtherDetails(otherDetails);
        leadRepositoryWrapper.saveWithException(lead);
        publishLeadUpdatedEvent(lead);
    }

    private void mergePropertyDetailsInto(UUID leadIdentifier,Lead.OtherDetails otherDetails, PatchPropertyDetailsRequest request) {
        if (request == null) {
            return;
        }
        Lead.PropertyDetails propertyDetails = otherDetails.getPropertyDetails();
        if (propertyDetails == null) {
            propertyDetails = new Lead.PropertyDetails();
        }
        if (request.getAddress() != null && request.getAddress().isPresent()) {
            mergeAddressInto( leadIdentifier, propertyDetails, request.getAddress().get());
        }
        if (request.getGeoData() != null) {
            propertyDetails.setGeoData(request.getGeoData().orElse(null));
        }
        if (request.getPropertyType() != null) {
            String value = request.getPropertyType().orElse(null);
            if (value != null && !value.isBlank()) {
                codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        value, SystemControlledMasterCodes.LEAD_ROOF_PROFILE_MASTER
                );
            }
            propertyDetails.setPropertyType(value);
        }
        if (request.getPropertyConstructionStage() != null) {
            String value = request.getPropertyConstructionStage().orElse(null);
            if (value != null && !value.isBlank()) {
                codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        value, SystemControlledMasterCodes.LEAD_PROPERTY_CONSTRUCTION_STATUS_MASTER
                );
            }
            propertyDetails.setPropertyConstructionStage(value);
        }
        if (request.getOwner() != null) {
            propertyDetails.setOwner(request.getOwner().orElse(null));
        }
        if (request.getOwnerRelation() != null) {
            propertyDetails.setOwnerRelation(request.getOwnerRelation().orElse(null));
        }
        if (request.getPropertyMeasurementDetails() != null) {
            var m = request.getPropertyMeasurementDetails().orElse(null);
            if (m != null) {
                propertyDetails.setPropertyMeasurementDetails(
                        Lead.PropertyDetails.PropertyMeasurementDetails.builder()
                                .buildUpArea(m.getBuildUpArea())
                                .siteArea(m.getSiteArea())
                                .build());
            } else {
                propertyDetails.setPropertyMeasurementDetails(null);
            }
        }
        if (request.getDocumentChecklist() != null) {
            mergeDocumentChecklistInto(propertyDetails, request.getDocumentChecklist().orElse(null));
        }
        otherDetails.setPropertyDetails(propertyDetails);
    }

    private void mergeAddressInto(UUID leadIdentifier, Lead.PropertyDetails propertyDetails, PatchAddressData patch) {
        AddressData existing = propertyDetails.getAddress();
        if (existing == null) {
            existing = new AddressData();
            propertyDetails.setAddress(existing);
        }
        if (patch.getId() != null && patch.getId().isPresent()) {
            existing.setId(patch.getId().get());
        }
        if (patch.getAddressType() != null && patch.getAddressType().isPresent()) {
            existing.setAddressType(patch.getAddressType().get());
        }
        if (patch.getAddress() != null && patch.getAddress().isPresent()) {
            existing.setAddress(patch.getAddress().get());
        }
        if (patch.getPincode() != null && patch.getPincode().isPresent()) {
            existing.setPincode(patch.getPincode().get());
        }
        if (patch.getDistrict() != null && patch.getDistrict().isPresent()) {
            existing.setDistrict(patch.getDistrict().get());
            analyticsHelper.captureLead(new AnalyticsEvent(leadIdentifier.toString(),"district_selected", List.of(new AnalyticsEvent.Param("district", existing.getDistrict()))));
        }
        if (patch.getCountry() != null && patch.getCountry().isPresent()) {
            existing.setCountry(patch.getCountry().get());
        }
        if (patch.getState() != null && patch.getState().isPresent()) {
            existing.setState(patch.getState().get());
        }
        if (patch.getRegion() != null && patch.getRegion().isPresent()) {
            existing.setRegion(patch.getRegion().get());
        }
        if (patch.getTaluka() != null && patch.getTaluka().isPresent()) {
            existing.setTaluka(patch.getTaluka().get());
        }
        if (patch.getDistrictCode() != null && patch.getDistrictCode().isPresent()) {
            existing.setDistrictCode(patch.getDistrictCode().get());
        }
        if (patch.getRegionCode() != null && patch.getRegionCode().isPresent()) {
            existing.setRegionCode(patch.getRegionCode().get());
        }
        if (patch.getStateCode() != null && patch.getStateCode().isPresent()) {
            existing.setStateCode(patch.getStateCode().get());
        }
        if (patch.getCountryCode() != null && patch.getCountryCode().isPresent()) {
            existing.setCountryCode(patch.getCountryCode().get());
        }
        if (patch.getTalukaCode() != null && patch.getTalukaCode().isPresent()) {
            existing.setTalukaCode(patch.getTalukaCode().get());
        }
        if (patch.getDistrictId() != null && patch.getDistrictId().isPresent()) {
            existing.setDistrictId(patch.getDistrictId().get());
        }
        if (patch.getRegionId() != null && patch.getRegionId().isPresent()) {
            existing.setRegionId(patch.getRegionId().get());
        }
        if (patch.getStateId() != null && patch.getStateId().isPresent()) {
            existing.setStateId(patch.getStateId().get());
        }
        if (patch.getCountryId() != null && patch.getCountryId().isPresent()) {
            existing.setCountryId(patch.getCountryId().get());
        }
        if (patch.getTalukaId() != null && patch.getTalukaId().isPresent()) {
            existing.setTalukaId(patch.getTalukaId().get());
        }
        if (patch.getVillageCode() != null && patch.getVillageCode().isPresent()) {
            existing.setVillageCode(patch.getVillageCode().get());
        }
        if (patch.getVillageId() != null && patch.getVillageId().isPresent()) {
            existing.setVillageId(patch.getVillageId().get());
        }
        if (patch.getVillageName() != null && patch.getVillageName().isPresent()) {
            existing.setVillageName(patch.getVillageName().get());
        }
        if (patch.getOperatingAreaName() != null && patch.getOperatingAreaName().isPresent()) {
            existing.setOperatingAreaName(patch.getOperatingAreaName().get());
        }
        if (patch.getOperatingAreaCode() != null && patch.getOperatingAreaCode().isPresent()) {
            existing.setOperatingAreaCode(patch.getOperatingAreaCode().get());
        }
        if (patch.getOperatingAreaId() != null && patch.getOperatingAreaId().isPresent()) {
            existing.setOperatingAreaId(patch.getOperatingAreaId().get());
        }
        if (patch.getIsServiceable() != null && patch.getIsServiceable().isPresent()) {
            existing.setIsServiceable(patch.getIsServiceable().get());
        }
    }

    private void mergeDocumentChecklistInto(Lead.PropertyDetails propertyDetails, PatchDocumentChecklistRequest request) {
        if (request == null) {
            return;
        }
        Lead.DocumentChecklist checklist = propertyDetails.getDocumentChecklist();
        if (checklist == null) {
            checklist = new Lead.DocumentChecklist();
        }
        if (request.getEkhataType() != null) {
            String value = request.getEkhataType().orElse(null);
            if (value != null && !value.isBlank()) {
                if (EkhataType.fromKey(value) == null) {
                    throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
                }
                checklist.setEkhataType(EkhataType.fromKey(value).getKey());
            } else {
                checklist.setEkhataType(null);
            }
        }
        if (request.getEkhataStatus() != null) {
            String value = request.getEkhataStatus().orElse(null);
            if (value != null && !value.isBlank()) {
                if (RegistrationStatus.fromKey(value) == null) {
                    throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
                }
                checklist.setEkhataStatus(RegistrationStatus.fromKey(value).getKey());
            } else {
                checklist.setEkhataStatus(null);
            }
        }
        if (request.getSaleDeed() != null) {
            String value = request.getSaleDeed().orElse(null);
            if (value != null && !value.isBlank()) {
                if (AvailabilityStatus.fromKey(value) == null) {
                    throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
                }
                checklist.setSaleDeed(AvailabilityStatus.fromKey(value).getKey());
            } else {
                checklist.setSaleDeed(null);
            }
        }
        if (request.getPropertyTax() != null) {
            String value = request.getPropertyTax().orElse(null);
            if (value != null && !value.isBlank()) {
                if (AvailabilityStatus.fromKey(value) == null) {
                    throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
                }
                checklist.setPropertyTax(AvailabilityStatus.fromKey(value).getKey());
            } else {
                checklist.setPropertyTax(null);
            }
        }
        if (request.getStatementOfAccounts() != null) {
            String value = request.getStatementOfAccounts().orElse(null);
            if (value != null && !value.isBlank()) {
                if (AvailabilityStatus.fromKey(value) == null) {
                    throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
                }
                checklist.setStatementOfAccounts(AvailabilityStatus.fromKey(value).getKey());
            } else {
                checklist.setStatementOfAccounts(null);
            }
        }
        if (request.getOtherDocs() != null) {
            String value = request.getOtherDocs().orElse(null);
            if (value != null && !value.isBlank()) {
                if (AvailabilityStatus.fromKey(value) == null) {
                    throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
                }
                checklist.setOtherDocs(AvailabilityStatus.fromKey(value).getKey());
            } else {
                checklist.setOtherDocs(null);
            }
        }
        propertyDetails.setDocumentChecklist(checklist);
    }

    private void mergeIncomeObligationDetailsInto(Lead lead, PatchIncomeAndObligationRequest request) {
        if (request == null) {
            return;
        }
        Lead.IncomeObligationDetails existing = lead.getIncomeObligationDetails();

        List<Lead.IncomeDetails> incomeDetails = null;
        if (request.getIncomeDetails() != null && request.getIncomeDetails().isPresent()) {
            List<PatchIncomeAndObligationRequest.IncomeDetailsData> fromRequest = request.getIncomeDetails().get();
            if (fromRequest != null) {
                for (PatchIncomeAndObligationRequest.IncomeDetailsData d : fromRequest) {
                    String incomeSource = d.getIncomeSource();
                    if (incomeSource != null && !incomeSource.isBlank()) {
                        codeValueMasterService.getByKey(incomeSource);
                    }
                    validateIncomeDocumentChecklist(d.getDocumentChecklist());
                }
                incomeDetails = fromRequest.stream()
                        .map(d -> Lead.IncomeDetails.builder()
                                .incomeSource(d.getIncomeSource())
                                .amount(d.getAmount())
                                .documentChecklist(mapIncomeDocumentChecklist(d.getDocumentChecklist()))
                                .build())
                        .toList();
            }
        } else if (existing != null && existing.getIncomeDetails() != null) {
            incomeDetails = existing.getIncomeDetails();
        }

        Lead.ObligationDetails obligationDetails = null;
        if (request.getObligationDetails() != null && request.getObligationDetails().isPresent()) {
            PatchIncomeAndObligationRequest.ObligationData data = request.getObligationDetails().orElse(null);
            obligationDetails = data == null ? null : Lead.ObligationDetails.builder()
                    .existingEmi(data.getExistingEmi())
                    .build();
        } else if (existing != null && existing.getObligationDetails() != null) {
            obligationDetails = existing.getObligationDetails();
        }

        BigDecimal monthlyFamilyIncome = null;
        if (request.getMonthlyFamilyIncome() != null && request.getMonthlyFamilyIncome().isPresent()) {
            monthlyFamilyIncome = request.getMonthlyFamilyIncome().orElse(null);
        } else if (existing != null && existing.getMonthlyFamilyIncome() != null) {
            monthlyFamilyIncome = existing.getMonthlyFamilyIncome();
        }

        Lead.IncomeObligationDetails newDetails = Lead.IncomeObligationDetails.builder()
                .incomeDetails(incomeDetails)
                .obligationDetails(obligationDetails)
                .monthlyFamilyIncome(monthlyFamilyIncome)
                .build();
        lead.setIncomeObligationDetails(newDetails);
    }

    private void validateIncomeDocumentChecklist(List<PatchIncomeAndObligationRequest.IncomeDocumentChecklistData> checklist) {
        if (checklist == null || checklist.isEmpty()) {
            return;
        }
        for (PatchIncomeAndObligationRequest.IncomeDocumentChecklistData item : checklist) {
            if (item.getDocumentType() == null || item.getDocumentType().isBlank()) {
                throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
            }
            codeValueMasterService.getByKey(item.getDocumentType());
            if (item.getStatus() == null || item.getStatus().isBlank()) {
                throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
            }
            if (AvailabilityStatus.fromKey(item.getStatus()) == null) {
                throw LeadExceptionFactory.invalidDocumentChecklistStatus(messageSource);
            }
        }
    }

    private List<Lead.IncomeDocumentChecklist> mapIncomeDocumentChecklist(
            List<PatchIncomeAndObligationRequest.IncomeDocumentChecklistData> checklist) {
        if (checklist == null || checklist.isEmpty()) {
            return null;
        }
        return checklist.stream()
                .map(item -> Lead.IncomeDocumentChecklist.builder()
                        .documentType(item.getDocumentType())
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    private void mergeCurrentCustomerFormStepInto(Lead.OtherDetails otherDetails, String value) {
        if (value != null && !value.isBlank()) {
            CustomerFormStep step = CustomerFormStep.fromKey(value.trim());
            if (step == null) {
                throw LeadExceptionFactory.invalidCustomerFormStep(messageSource);
            }
            otherDetails.setCurrentCustomerFormStep(step.getKey());
        } else {
            otherDetails.setCurrentCustomerFormStep(null);
        }
    }

    @Override
    @Transactional
    public void deleteLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierIncludingDeletedWithException(leadIdentifier);

        if (Boolean.TRUE.equals(lead.getIsDeleted())) {
            throw LeadExceptionFactory.leadAlreadyDeleted(leadIdentifier, messageSource);
        }

        lead.setIsDeleted(true);
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void undoDeleteLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierIncludingDeletedWithException(leadIdentifier);

        if (!Boolean.TRUE.equals(lead.getIsDeleted())) {
            throw LeadExceptionFactory.leadNotDeleted(leadIdentifier, messageSource);
        }

        lead.setIsDeleted(false);
        leadRepositoryWrapper.saveWithException(lead);
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

    private void handleSourcingChannel(Lead lead, SourcingChannelRequest sourcingChannelRequest) {
        if (ValidationUtils.isNull(sourcingChannelRequest)) {
            return;
        }
        if (lead.getSourcingChannelId() != null) {
            sourcingChannelWriteService.update(lead.getSourcingChannelId(), sourcingChannelRequest);
        } else {
            SourcingChannelResponse response = sourcingChannelWriteService.create(sourcingChannelRequest);
            if (response != null && response.getId() != null) {
                lead.setSourcingChannelId(response.getId());
                leadRepositoryWrapper.saveWithException(lead);
            }
        }
    }
}
