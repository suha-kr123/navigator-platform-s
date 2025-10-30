package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.ContactPersonType;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.repository.ApplicantRepository;
import com.nivasafinance.features.lead.repository.ContactRepository;
import com.nivasafinance.features.lead.repository.LeadRepository;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class LeadWriteServiceImpl implements LeadWriteService {

    private final LeadRepository leadRepository;
    private final ContactRepository contactRepository;
    private final ApplicantRepository applicantRepository;
    private final PersonService personService;

    @Override
    public CreateLeadResponse createLead(CreateLeadRequest request) {
        // TODO: Check if product exists in product module
        // ProductService productService = ...;
        // productService.validateProductExists(request.getProduct());


        // Create person with phoneNo
        MobileNumberDetails mobileNumber = new MobileNumberDetails();
        mobileNumber.setNumber(request.getPhoneNumber().getMobileNumber());
        mobileNumber.setPrimary(true);
        mobileNumber.setWhatsappAvailable(request.getPhoneNumber().isWhatsapp());

        List<MobileNumberDetails> mobileNumbers = new ArrayList<>();
        mobileNumbers.add(mobileNumber);

        PersonCreateRequest personCreateRequest = new PersonCreateRequest(
                null, // firstName
                null, // middleName
                null, // lastName
                mobileNumbers,
                null, // dateOfBirth
                null, // gender
                null  // extData
        );

        PersonResponse personResponse = personService.createPerson(personCreateRequest);

        // Create contact
        Contact contact = new Contact();
        contact.setIdentifier(UUID.randomUUID());
        contact.setPersonId(personResponse.getId());
        Contact savedContact = contactRepository.save(contact);

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

        Lead savedLead = leadRepository.save(lead);

        return new CreateLeadResponse(savedLead.getLeadIdentifier());
    }
}
