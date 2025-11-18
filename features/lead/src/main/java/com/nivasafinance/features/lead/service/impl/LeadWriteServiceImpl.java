package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCreationEventPayload;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.ActiveLeadAlreadyExistsException;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
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

        contactWriteService.createContact(savedLead.getLeadIdentifier(), contactPersonDetails);

        publishLeadCreatedEvent(savedLead, request);

        return new CreateLeadResponse(savedLead.getLeadIdentifier());
    }

    @Override
    @Transactional
    public void updateLead(UUID leadIdentifier, UpdateLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        lead.setRequestedAmount(request.getRequestedAmount());
        lead.setOfficeKey(request.getOfficeKey());
        lead.setOwner(request.getOwner());
        lead.setPurpose(request.getPurpose());
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

        leadRepositoryWrapper.saveWithException(lead);
    }

    private void publishLeadCreatedEvent(Lead lead, CreateLeadRequest request) {
        String mobileNumber = request.getPhoneNumber() != null
                ? request.getPhoneNumber().getMobileNumber()
                : null;

        LeadCreationEventPayload payload = LeadCreationEventPayload.builder()
                .leadId(lead.getLeadIdentifier())
                .mobileNumber(mobileNumber)
                .build();

        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_CREATED.toString(), payload)
        );
    }

    @Override
    public void touchLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
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

        creditDetails.setOccupationProfile(request.getOccupationProfile());
        creditDetails.setRoofProfile(request.getRoofProfile());
        creditDetails.setLtv(request.getLtv());
        creditDetails.setFoir(request.getFoir());
        creditDetails.setMonthlyFamilyIncome(request.getMonthlyFamilyIncome());
        creditDetails.setPropertyDocumentType(request.getPropertyDocumentType());
        creditDetails.setEligibleLoanAmount(request.getEligibleLoanAmount());
        creditDetails.setLocation(request.getLocation());
        creditDetails.setBureauRating(request.getBureauRating());
        creditDetails.setCustomerProfiles(request.getCustomerProfiles());
        creditDetails.setUnderwriter(currentUsername);

        lead.setCreditRatingDetails(creditDetails);
        leadRepositoryWrapper.saveWithException(lead);
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
    }

    @Override
    @Transactional
    public void createTranche(UUID leadIdentifier, CreateTrancheRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate that disbursement details exist
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null) {
            throw new RuntimeException("Disbursement details must exist before adding tranches");
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
    }

    @Override
    @Transactional
    public void updateTranche(UUID leadIdentifier, UUID trancheIdentifier, UpdateTrancheRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate that disbursement details exist
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null || disbursementDetails.getTranches() == null) {
            throw new RuntimeException("Tranche not found with identifier: " + trancheIdentifier);
        }

        // Find tranche by identifier
        Lead.Tranche tranche = disbursementDetails.getTranches().stream()
                .filter(t -> trancheIdentifier.equals(t.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tranche not found with identifier: " + trancheIdentifier));

        // Update tranche fields (PUT semantics - full replacement)
        tranche.setAmount(request.getAmount());
        tranche.setDate(request.getDate());

        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void deleteTranche(UUID leadIdentifier, UUID trancheIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate that disbursement details exist
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();
        if (disbursementDetails == null || disbursementDetails.getTranches() == null) {
            throw new RuntimeException("Tranche not found with identifier: " + trancheIdentifier);
        }

        // Find and remove tranche by identifier
        List<Lead.Tranche> tranches = disbursementDetails.getTranches();
        boolean removed = tranches.removeIf(t -> trancheIdentifier.equals(t.getIdentifier()));

        if (!removed) {
            throw new RuntimeException("Tranche not found with identifier: " + trancheIdentifier);
        }

        leadRepositoryWrapper.saveWithException(lead);
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
    }

    @Override
    @Transactional
    public void onholdLead(UUID leadIdentifier, OnholdLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Validate lead is not rejected or withdrawn
        if (LeadStatus.REJECTED.equals(lead.getStatus()) || LeadStatus.WITHDRAWN.equals(lead.getStatus())) {
            throw new BadRequestException("Cannot put lead on hold, lead is already rejected or withdrawn");
        }

        // Set substatus to ONHOLD (keep current status)
        lead.setSubstatus(LeadSubStatus.ONHOLD);

        // Store reason code if provided
        if (request.getReasonCode() != null) {
            //validate reason
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getReasonCode(), SystemControlledMasterCodes.LEAD_DROPOFF_REASON_MASTER);
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setOnhold(request.getReasonCode());
            lead.setReasons(reasons);
        }

        leadRepositoryWrapper.saveWithException(lead);
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
}
