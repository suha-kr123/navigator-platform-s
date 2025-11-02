package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.GeoData;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.UpdateCreditDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePropertyDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateProposedDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonService;
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
    private final PersonService personService;
    private final MessageSource messageSource;
    private final PincodeService pincodeService;
    private final SourcingChannelWriteService sourcingChannelWriteService;


    @Override
    @Transactional
    public CreateLeadResponse createLead(CreateLeadRequest request) {
        // TODO: Check if product exists in product module
        // ProductService productService = ...;
        // productService.validateProductExists(request.getProduct());

        //TODO : Check if active lead already exists with this phone number
       /* if (existingActiveLead.isPresent()) {
            throw new ActiveLeadAlreadyExistsException(request.getPhoneNumber().getMobileNumber(), messageSource);
        }*/
        PersonCreateRequest personCreateRequest = getPersonCreateRequest(request);

        PersonResponse personResponse = personService.createPerson(personCreateRequest);

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
        lead.setStatus(LeadStatus.ENQUIRY); // Using ENQUIRY as IN_PROGRESS status

        // Set contact details
        Lead.ContactDetails contactDetails = new Lead.ContactDetails();
        contactDetails.setContactId(savedContact.getId());
        List<Lead.ContactDetails> contactDetailsList = new ArrayList<>();
        contactDetailsList.add(contactDetails);
        lead.setContactDetails(contactDetailsList);

        Lead savedLead = leadRepositoryWrapper.saveWithException(lead);

        return new CreateLeadResponse(savedLead.getLeadIdentifier());
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

        AddressData addressData = new AddressData();
        addressData.setAddressLineOne(request.getAddress().getAddressLineOne());
        addressData.setAddressLineTwo(request.getAddress().getAddressLineTwo());
        addressData.setPincode(request.getAddress().getPincode());
        addressData.setCountry(null); // to set to default
        addressData.setState(null);
        addressData.setDistrict(null);
        addressData.setIsServiceable(false);

        // Try to fetch pincode data
        try {
            PincodeResponse pincodeResponse = pincodeService.getPincodeDetails(request.getAddress().getPincode());
            addressData.setDistrict(pincodeResponse.getDistrict());
            addressData.setState(pincodeResponse.getState());
            addressData.setCountry(pincodeResponse.getCountry());
            addressData.setIsServiceable(pincodeResponse.isServicable());
        } catch (Exception e) {
            log.warn("Failed to fetch pincode details for pincode: {}. Continuing with null values.",
                    request.getAddress().getPincode(), e);
        }
        addressData.setArea(request.getAddress().getArea());
        
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
                null, // gender
                null  // extData
        );
    }

    @NotNull
    private List<MobileNumberDetails> getMobileNumberDetails(CreateLeadRequest request) {
        // Create person with phoneNo
        MobileNumberDetails mobileNumber = new MobileNumberDetails();
        mobileNumber.setNumber(request.getPhoneNumber().getMobileNumber());
        mobileNumber.setPrimary(true);
        mobileNumber.setWhatsappAvailable(request.getPhoneNumber().isWhatsapp());

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
}
