package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonService;
import lombok.AllArgsConstructor;
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
public class LeadWriteServiceImpl implements LeadWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final ApplicantRepositoryWrapper applicantRepositoryWrapper;
    private final PersonService personService;
    private final MessageSource messageSource;


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
}
