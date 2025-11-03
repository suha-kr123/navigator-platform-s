package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.CreateTrancheRequest;
import com.nivasafinance.features.lead.dto.OnholdLeadRequest;
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.dto.UpdateCreditDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateDisbursementDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadRequest;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePropertyDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateProposedDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateTrancheRequest;
import com.nivasafinance.features.lead.dto.WithdrawLeadRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import com.nivasafinance.security.context.UserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class LeadWriteServiceImpl implements LeadWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final ApplicantRepositoryWrapper applicantRepositoryWrapper;
    private final PersonWriteService personWriteService;
    private final MessageSource messageSource;
    private final AddressDataService addressDataService;
    private final SourcingChannelWriteService sourcingChannelWriteService;
    private final ProductReadService productReadService;


    @Override
    @Transactional
    public CreateLeadResponse createLead(CreateLeadRequest request) {
        //validates product exists
        productReadService.getProductByCode(request.getProduct());

        //TODO : Check if active lead already exists with this phone number
       /* if (existingActiveLead.isPresent()) {
            throw new ActiveLeadAlreadyExistsException(request.getPhoneNumber().getMobileNumber(), messageSource);
        }*/
        PersonCreateRequest personCreateRequest = getPersonCreateRequest(request);

        PersonCreateResponse personResponse = personWriteService.createPerson(personCreateRequest);

        // Create contact
        Contact contact = new Contact();
        contact.setIdentifier(UUID.randomUUID());
        contact.setPersonId(personResponse.getId());
        Contact savedContact = contactRepositoryWrapper.saveWithException(contact);

        // Create lead
        Lead lead = new Lead();
        lead.setLeadIdentifier(UUID.randomUUID());
        lead.setRequestedAmount(request.getRequestedLoanAmount());
        lead.setProductCode(request.getProduct());
        lead.setStatus(LeadStatus.ENQUIRY);

        // Set contact to lead
        lead.setContacts(List.of(savedContact.getId()));

        Lead savedLead = leadRepositoryWrapper.saveWithException(lead);

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
        productReadService.getProductByCode(request.getProductCode());
        lead.setProductCode(request.getProductCode());
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void updatePreliminaryDetails(UUID leadIdentifier, UpdatePreliminaryDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        lead.setPreliminaryDetails(request.getPreliminaryDetails());
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void updateCreditDetails(UUID leadIdentifier, UpdateCreditDetailsRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Get current username from UserContext
        String currentUsername = UserContext.getUserInfo().getUsername();

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

    @NotNull
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

    @NotNull
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
                new SourcingChannelRequest.MarketingDetails(request.getSourceId())
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
        
        // Store reason code if provided
        if (request.getReasonCode() != null) {
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setReject(request.getReasonCode());
            lead.setReasons(reasons);
        }
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void withdrawLead(UUID leadIdentifier, WithdrawLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Set status to WITHDRAWN
        lead.setStatus(LeadStatus.WITHDRAWN);
        
        // Store reason code if provided
        if (request.getReasonCode() != null) {
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setWithdrawn(request.getReasonCode());
            lead.setReasons(reasons);
        }
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void submitLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Set status to SUBMITTED
        lead.setStatus(LeadStatus.SUBMITTED);
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void disburseLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Set status to DISBURSED
        lead.setStatus(LeadStatus.DISBURSED);
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void completeLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Set status to COMPLETED
        lead.setStatus(LeadStatus.COMPLETED);
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void qualifyLead(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Set status to QUALIFIED
        lead.setStatus(LeadStatus.QUALIFIED);
        
        leadRepositoryWrapper.saveWithException(lead);
    }

    @Override
    @Transactional
    public void onholdLead(UUID leadIdentifier, OnholdLeadRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Set substatus to ONHOLD (keep current status)
        lead.setSubstatus(LeadSubStatus.ONHOLD);
        
        // Store reason code if provided
        if (request.getReasonCode() != null) {
            Lead.ReasonDetails reasons = lead.getReasons();
            if (reasons == null) {
                reasons = new Lead.ReasonDetails();
            }
            reasons.setOnhold(request.getReasonCode());
            lead.setReasons(reasons);
        }
        
        leadRepositoryWrapper.saveWithException(lead);
    }
}
