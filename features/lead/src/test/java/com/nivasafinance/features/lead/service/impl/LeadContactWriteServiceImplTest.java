package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Applicant;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import com.nivasafinance.features.lead.exception.ContactNotFoundException;
import com.nivasafinance.features.lead.exception.LeadContactValidationException;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.enums.Gender;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadContactWriteServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private ApplicantRepositoryWrapper applicantRepositoryWrapper;

    @Mock
    private PersonWriteService personWriteService;

    @Mock
    private PersonReadService personReadService;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private LeadContactReadService leadContactReadService;

    @Mock
    private ReferralCodeRegistryService referralCodeRegistryService;

    @InjectMocks
    private LeadContactWriteServiceImpl leadContactWriteService;

    private UUID leadIdentifier;
    private Long leadId;
    private Lead lead;
    private Contact contact;
    private UUID contactIdentifier;
    private Long contactId;
    private Long personId;

    @BeforeEach
    void setUp() {
        UserContext.setUsername("test-user");

        leadIdentifier = UUID.randomUUID();
        leadId = 1L;
        contactIdentifier = UUID.randomUUID();
        contactId = 10L;
        personId = 100L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setContacts(new ArrayList<>(List.of(contactId)));
        lead.setOtherDetails(Lead.OtherDetails.builder().build());

        contact = new Contact();
        contact.setId(contactId);
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);
        contact.setIsDecisionMaker(false);
        contact.setIsPropertyOwner(false);
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ==================== createContact() Tests ====================

    @Test
    void createContact_withNewPerson_createsContactAndReturnsIdentifier() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());

        PersonCreateResponse personCreateResponse = PersonCreateResponse.builder().id(personId).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(personCreateResponse);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertNotNull(result.getIdentifier(), "Identifier should be generated");
        verify(personWriteService).createPerson(any());
        verify(contactRepositoryWrapper).saveWithException(any(Contact.class));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createContact_withExistingPerson_reusesPersonId() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());

        Person existingPerson = new Person();
        existingPerson.setId(personId);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.of(existingPerson));
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(personWriteService, never()).createPerson(any());
    }

    @Test
    void createContact_withDuplicatePersonOnLead_throwsValidationException() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>(List.of(contactId)));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.createContact(leadIdentifier, request),
                "Should throw when lead already has a contact with the same personId");
    }

    @Test
    void createContact_withApplicantType_createsApplicantRecord() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());
        lead.setApplicant(null);

        ReferralCodeRegistryResponse referralResponse = new ReferralCodeRegistryResponse();
        referralResponse.setReferralCode("REF-001");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(referralResponse);
        when(applicantRepositoryWrapper.saveWithException(any(Applicant.class))).thenAnswer(inv -> {
            Applicant a = inv.getArgument(0);
            a.setId(50L);
            return a;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(applicantRepositoryWrapper).saveWithException(any(Applicant.class));
    }

    @Test
    void createContact_withDecisionMaker_unsetsOtherDecisionMakers() {
        // Arrange
        Long existingContactId = 20L;
        Contact existingContact = new Contact();
        existingContact.setId(existingContactId);
        existingContact.setPersonId(200L);
        existingContact.setIsDecisionMaker(true);

        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(true).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>(List.of(existingContactId)));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.findByIdWithException(existingContactId)).thenReturn(existingContact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            if (c.getId() == null) c.setId(contactId);
            return c;
        });
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertFalse(existingContact.getIsDecisionMaker(),
                "Existing decision maker should be unset when new contact is decision maker");
    }

    @Test
    void createContact_withNullMobileNumbers_createsNewPerson() {
        // Arrange
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(null).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should create contact even with null mobile numbers");
        verify(personWriteService).createPerson(any());
    }

    // ==================== updateContact() Tests ====================

    @Test
    void updateContact_withValidData_updatesPersonAndContact() {
        // Arrange
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Updated").lastName("Name").gender(Gender.MALE).build();
        UpdateLeadContactRequest request = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(true).build();

        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.updateContact(leadIdentifier, contactIdentifier, request);

        // Assert
        verify(personWriteService).updatePerson(eq(personId), any(PersonUpdateRequest.class));
        assertTrue(contact.getIsPropertyOwner(), "Property owner flag should be updated");
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void updateContact_withApplicantTypeChange_removesOldAndAddsNew() {
        // Arrange
        Long oldApplicantId = 50L;
        Applicant oldApplicant = new Applicant();
        oldApplicant.setId(oldApplicantId);
        oldApplicant.setPersonId(personId);

        lead.setApplicant(oldApplicantId);
        lead.setCoApplicants(null);

        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").build();
        UpdateLeadContactRequest request = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.CO_APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        ReferralCodeRegistryResponse referralResponse = new ReferralCodeRegistryResponse();
        referralResponse.setReferralCode("REF-002");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(applicantRepositoryWrapper.findByIdWithException(oldApplicantId)).thenReturn(oldApplicant);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(referralResponse);
        when(applicantRepositoryWrapper.saveWithException(any(Applicant.class))).thenAnswer(inv -> {
            Applicant a = inv.getArgument(0);
            a.setId(60L);
            return a;
        });
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.updateContact(leadIdentifier, contactIdentifier, request);

        // Assert
        verify(applicantRepositoryWrapper).delete(oldApplicant);
        verify(applicantRepositoryWrapper).saveWithException(any(Applicant.class));
        assertNull(lead.getApplicant(), "Old applicant should be removed from lead");
    }

    @Test
    void updateContact_withNonExistentContact_throwsContactNotFoundException() {
        // Arrange
        UUID unknownContactId = UUID.randomUUID();
        Contact otherContact = new Contact();
        otherContact.setId(99L);

        lead.setContacts(new ArrayList<>(List.of(contactId)));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(unknownContactId)).thenReturn(otherContact);

        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder().firstName("X").build();
        UpdateLeadContactRequest request = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).build();

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactWriteService.updateContact(leadIdentifier, unknownContactId, request),
                "Should throw when contact is not part of the lead");
    }

    // ==================== updateContactName() Tests ====================

    @Test
    void updateContactName_withNewFirstName_mergesWithExisting() {
        // Arrange
        PersonResponse existingPerson = PersonResponse.builder()
                .firstName("OldFirst").middleName("OldMiddle").lastName("OldLast")
                .email("test@test.com").gender(Gender.MALE).dateOfBirth(LocalDate.of(1990, 1, 1)).build();

        UpdateContactNameRequest request = UpdateContactNameRequest.builder()
                .firstName("NewFirst").middleName(null).lastName(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(existingPerson);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        // Act
        leadContactWriteService.updateContactName(leadIdentifier, contactIdentifier, request);

        // Assert
        ArgumentCaptor<PersonUpdateRequest> captor = ArgumentCaptor.forClass(PersonUpdateRequest.class);
        verify(personWriteService).updatePerson(eq(personId), captor.capture());

        PersonUpdateRequest captured = captor.getValue();
        assertEquals("NewFirst", captured.getFirstName(), "First name should be updated to new value");
        assertEquals("OldMiddle", captured.getMiddleName(), "Middle name should retain existing value when null in request");
        assertEquals("OldLast", captured.getLastName(), "Last name should retain existing value when null in request");
    }

    @Test
    void updateContactName_setsContactAsDecisionMaker() {
        // Arrange
        PersonResponse existingPerson = PersonResponse.builder()
                .firstName("Old").lastName("Name").build();
        UpdateContactNameRequest request = UpdateContactNameRequest.builder()
                .firstName("New").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(existingPerson);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        // Act
        leadContactWriteService.updateContactName(leadIdentifier, contactIdentifier, request);

        // Assert
        assertTrue(contact.getIsDecisionMaker(), "Contact should be set as decision maker after name update");
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    // ==================== deleteContact() Tests ====================

    @Test
    void deleteContact_withMultipleContacts_removesContact() {
        // Arrange
        Long contactId2 = 20L;
        Contact contact2 = new Contact();
        contact2.setId(contactId2);
        contact2.setIdentifier(UUID.randomUUID());
        contact2.setPersonId(200L);
        contact2.setIsDecisionMaker(false);

        lead.setContacts(new ArrayList<>(List.of(contactId, contactId2)));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId2)).thenReturn(contact2);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.deleteContact(leadIdentifier, contactIdentifier);

        // Assert
        assertFalse(lead.getContacts().contains(contactId), "Deleted contact should be removed from lead's contacts list");
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void deleteContact_withOnlyOneContact_throwsRuntimeException() {
        // Arrange
        lead.setContacts(new ArrayList<>(List.of(contactId)));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadContactWriteService.deleteContact(leadIdentifier, contactIdentifier),
                "Should throw when trying to delete the last contact");
    }

    @Test
    void deleteContact_withNullContacts_throwsRuntimeException() {
        // Arrange
        lead.setContacts(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadContactWriteService.deleteContact(leadIdentifier, contactIdentifier),
                "Should throw when contacts list is null");
    }

    @Test
    void deleteContact_withApplicantType_removesApplicantRecord() {
        // Arrange
        Long contactId2 = 20L;
        Contact contact2 = new Contact();
        contact2.setId(contactId2);
        contact2.setIdentifier(UUID.randomUUID());
        contact2.setPersonId(200L);
        contact2.setIsDecisionMaker(false);

        Long applicantId = 50L;
        Applicant applicant = new Applicant();
        applicant.setId(applicantId);
        applicant.setPersonId(personId);

        lead.setContacts(new ArrayList<>(List.of(contactId, contactId2)));
        lead.setApplicant(applicantId);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(applicantRepositoryWrapper.findByIdWithException(applicantId)).thenReturn(applicant);
        when(contactRepositoryWrapper.findByIdWithException(contactId2)).thenReturn(contact2);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.deleteContact(leadIdentifier, contactIdentifier);

        // Assert
        verify(applicantRepositoryWrapper).delete(applicant);
        assertNull(lead.getApplicant(), "Applicant reference should be cleared from lead");
    }

    // ==================== addAddress() Tests ====================

    @Test
    void addAddress_withValidRequest_delegatesToPersonWriteService() {
        // Arrange
        AddressRequest request = new AddressRequest();
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personWriteService.addAddress(personId, request)).thenReturn("addr-1");

        // Act
        String result = leadContactWriteService.addAddress(contactIdentifier, request);

        // Assert
        assertEquals("addr-1", result, "Should return address ID from person write service");
        verify(personWriteService).addAddress(personId, request);
    }

    // ==================== updateAddress() Tests ====================

    @Test
    void updateAddress_withValidRequest_delegatesToPersonWriteService() {
        // Arrange
        String addressId = "addr-1";
        AddressRequest request = new AddressRequest();
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act
        leadContactWriteService.updateAddress(contactIdentifier, addressId, request);

        // Assert
        verify(personWriteService).updateAddress(personId, addressId, request);
    }

    // ==================== addIdentifier() Tests ====================

    @Test
    void addIdentifier_withValidRequest_delegatesToPersonWriteService() {
        // Arrange
        IdentifierRequest request = new IdentifierRequest();
        IdentifierData expected = IdentifierData.builder().build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personWriteService.addIdentifier(personId, request)).thenReturn(expected);

        // Act
        IdentifierData result = leadContactWriteService.addIdentifier(leadIdentifier, contactIdentifier, request);

        // Assert
        assertNotNull(result, "Should return identifier data from person write service");
        verify(personWriteService).addIdentifier(personId, request);
    }

    @Test
    void addIdentifier_withContactNotOnLead_throwsContactNotFoundException() {
        // Arrange
        UUID unknownContactId = UUID.randomUUID();
        Contact otherContact = new Contact();
        otherContact.setId(99L);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(unknownContactId)).thenReturn(otherContact);

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactWriteService.addIdentifier(leadIdentifier, unknownContactId, new IdentifierRequest()),
                "Should throw when contact does not belong to the lead");
    }

    // ==================== updateIdentifier() Tests ====================

    @Test
    void updateIdentifier_withValidRequest_delegatesToPersonWriteService() {
        // Arrange
        UUID identifierId = UUID.randomUUID();
        IdentifierRequest request = new IdentifierRequest();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act
        leadContactWriteService.updateIdentifier(leadIdentifier, contactIdentifier, identifierId, request);

        // Assert
        verify(personWriteService).updateIdentifier(personId, identifierId, request);
    }

    // ==================== deleteIdentifier() Tests ====================

    @Test
    void deleteIdentifier_withValidRequest_delegatesToPersonWriteService() {
        // Arrange
        UUID identifierId = UUID.randomUUID();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act
        leadContactWriteService.deleteIdentifier(leadIdentifier, contactIdentifier, identifierId);

        // Assert
        verify(personWriteService).deleteIdentifier(personId, identifierId);
    }

    @Test
    void deleteIdentifier_withContactNotOnLead_throwsContactNotFoundException() {
        // Arrange
        UUID unknownContactId = UUID.randomUUID();
        UUID identifierId = UUID.randomUUID();
        Contact otherContact = new Contact();
        otherContact.setId(99L);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(unknownContactId)).thenReturn(otherContact);

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactWriteService.deleteIdentifier(leadIdentifier, unknownContactId, identifierId),
                "Should throw when contact does not belong to the lead");
    }

    // ==================== createContact() - CoApplicant Tests ====================

    @Test
    void createContact_withCoApplicantType_createsCoApplicantRecord() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Jane").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.CO_APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());
        lead.setCoApplicants(null);

        ReferralCodeRegistryResponse referralResponse = new ReferralCodeRegistryResponse();
        referralResponse.setReferralCode("REF-003");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(referralResponse);
        when(applicantRepositoryWrapper.saveWithException(any(Applicant.class))).thenAnswer(inv -> {
            Applicant a = inv.getArgument(0);
            a.setId(60L);
            return a;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertNotNull(lead.getCoApplicants(), "Co-applicants list should be initialized");
        assertFalse(lead.getCoApplicants().isEmpty(), "Co-applicants list should contain the new co-applicant");
    }

    // ==================== createContact() - Referral code failure ====================

    @Test
    void createContact_withApplicantTypeAndNullReferralCode_throwsValidationException() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());
        lead.setApplicant(null);

        ReferralCodeRegistryResponse referralResponse = new ReferralCodeRegistryResponse();
        referralResponse.setReferralCode(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(referralResponse);

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.createContact(leadIdentifier, request),
                "Should throw when referral code generation returns null code");
    }

    @Test
    void createContact_withApplicantTypeAndNullReferralResponse_throwsValidationException() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());
        lead.setApplicant(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.createContact(leadIdentifier, request),
                "Should throw when referral code service returns null response");
    }

    // ==================== updateContact() - decision maker unset ====================

    @Test
    void updateContact_withDecisionMakerTrue_unsetsOtherDecisionMakers() {
        // Arrange
        Long otherContactId = 20L;
        Contact otherContact = new Contact();
        otherContact.setId(otherContactId);
        otherContact.setPersonId(200L);
        otherContact.setIsDecisionMaker(true);

        lead.setContacts(new ArrayList<>(List.of(contactId, otherContactId)));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").build();
        UpdateLeadContactRequest request = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(true).isPropertyOwner(false).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(otherContactId)).thenReturn(otherContact);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.updateContact(leadIdentifier, contactIdentifier, request);

        // Assert
        assertFalse(otherContact.getIsDecisionMaker(),
                "Other contacts should have decision maker unset");
        assertTrue(contact.getIsDecisionMaker(),
                "Current contact should be set as decision maker");
    }

    // ==================== createContact() – additional coverage ====================

    @Test
    void createContact_withNullContactsList_initializesListAndAddsContact() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should create contact even when contacts list is initially null");
        assertNotNull(lead.getContacts(), "Contacts list should be initialized");
        assertTrue(lead.getContacts().contains(contactId), "Contact should be added to the list");
    }

    @Test
    void createContact_withExistingApplicant_replacesApplicantRecord() {
        // Arrange
        Long oldApplicantId = 50L;
        Applicant oldApplicant = new Applicant();
        oldApplicant.setId(oldApplicantId);
        oldApplicant.setPersonId(999L);

        lead.setContacts(new ArrayList<>());
        lead.setApplicant(oldApplicantId);

        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        ReferralCodeRegistryResponse referralResponse = new ReferralCodeRegistryResponse();
        referralResponse.setReferralCode("REF-010");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(applicantRepositoryWrapper.findByIdWithException(oldApplicantId)).thenReturn(oldApplicant);
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(referralResponse);
        when(applicantRepositoryWrapper.saveWithException(any(Applicant.class))).thenAnswer(inv -> {
            Applicant a = inv.getArgument(0);
            a.setId(60L);
            return a;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should create contact successfully");
        verify(applicantRepositoryWrapper).delete(oldApplicant);
        verify(applicantRepositoryWrapper).saveWithException(any(Applicant.class));
    }

    @Test
    void createContact_withEmptyMobileNumbers_createsNewPerson() {
        // Arrange
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of()).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should create contact with empty mobile numbers");
        verify(personWriteService).createPerson(any());
    }

    @Test
    void createContact_withNonPrimaryMobileNumber_createsNewPerson() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(false).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        CreateLeadContactResponse result = leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should create contact when no primary mobile number found");
        verify(personWriteService).createPerson(any());
        verify(personRepositoryWrapper, never()).findByPrimaryMobileNumber(anyString());
    }

    // ==================== updateContact() – additional coverage ====================

    @Test
    void updateContact_withSameApplicantType_skipsApplicantTypeChange() {
        // Arrange
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").build();
        UpdateLeadContactRequest request = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.updateContact(leadIdentifier, contactIdentifier, request);

        // Assert
        verify(applicantRepositoryWrapper, never()).delete(any());
        verify(applicantRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void updateContact_withCoApplicantToCurrent_determinesCorrectType() {
        // Arrange
        Long coApplicantId = 60L;
        Applicant coApplicant = new Applicant();
        coApplicant.setId(coApplicantId);
        coApplicant.setPersonId(personId);

        lead.setApplicant(null);
        lead.setCoApplicants(new ArrayList<>(List.of(coApplicantId)));

        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").build();
        UpdateLeadContactRequest request = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(applicantRepositoryWrapper.findByIdWithException(coApplicantId)).thenReturn(coApplicant);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.updateContact(leadIdentifier, contactIdentifier, request);

        // Assert
        verify(applicantRepositoryWrapper).delete(coApplicant);
    }

    // ==================== deleteContact() – additional coverage ====================

    @Test
    void deleteContact_withCoApplicantType_removesCoApplicantRecord() {
        // Arrange
        Long contactId2 = 20L;
        Contact contact2 = new Contact();
        contact2.setId(contactId2);
        contact2.setIdentifier(UUID.randomUUID());
        contact2.setPersonId(200L);
        contact2.setIsDecisionMaker(false);

        Long coApplicantId = 60L;
        Applicant coApplicant = new Applicant();
        coApplicant.setId(coApplicantId);
        coApplicant.setPersonId(personId);

        lead.setContacts(new ArrayList<>(List.of(contactId, contactId2)));
        lead.setApplicant(null);
        lead.setCoApplicants(new ArrayList<>(List.of(coApplicantId)));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(applicantRepositoryWrapper.findByIdWithException(coApplicantId)).thenReturn(coApplicant);
        when(contactRepositoryWrapper.findByIdWithException(contactId2)).thenReturn(contact2);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.deleteContact(leadIdentifier, contactIdentifier);

        // Assert
        verify(applicantRepositoryWrapper).delete(coApplicant);
        assertFalse(lead.getCoApplicants().contains(coApplicantId),
                "Co-applicant should be removed from list");
    }

    // ==================== updateContactName() – additional coverage ====================

    @Test
    void updateContactName_withContactNotOnLead_throwsContactNotFoundException() {
        // Arrange
        UUID unknownContactId = UUID.randomUUID();
        Contact otherContact = new Contact();
        otherContact.setId(99L);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(unknownContactId)).thenReturn(otherContact);

        UpdateContactNameRequest request = UpdateContactNameRequest.builder()
                .firstName("New").build();

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactWriteService.updateContactName(leadIdentifier, unknownContactId, request),
                "Should throw when contact is not part of the lead");
    }

    @Test
    void updateContactName_withAllNameFieldsProvided_usesAllNewValues() {
        // Arrange
        PersonResponse existingPerson = PersonResponse.builder()
                .firstName("OldFirst").middleName("OldMiddle").lastName("OldLast").build();

        UpdateContactNameRequest request = UpdateContactNameRequest.builder()
                .firstName("NewFirst").middleName("NewMiddle").lastName("NewLast").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(existingPerson);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        // Act
        leadContactWriteService.updateContactName(leadIdentifier, contactIdentifier, request);

        // Assert
        ArgumentCaptor<PersonUpdateRequest> captor = ArgumentCaptor.forClass(PersonUpdateRequest.class);
        verify(personWriteService).updatePerson(eq(personId), captor.capture());

        PersonUpdateRequest captured = captor.getValue();
        assertEquals("NewFirst", captured.getFirstName(), "First name should use new value");
        assertEquals("NewMiddle", captured.getMiddleName(), "Middle name should use new value");
        assertEquals("NewLast", captured.getLastName(), "Last name should use new value");
    }

    // ==================== updatePrimaryContactId – additional coverage ====================

    @Test
    void createContact_withNoDecisionMaker_primaryContactFallsBackToLastContact() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());
        lead.setOtherDetails(null);

        Contact noDecisionMaker = new Contact();
        noDecisionMaker.setId(contactId);
        noDecisionMaker.setIdentifier(contactIdentifier);
        noDecisionMaker.setPersonId(personId);
        noDecisionMaker.setIsDecisionMaker(false);
        noDecisionMaker.setIsPropertyOwner(false);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(noDecisionMaker);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getOtherDetails(), "OtherDetails should be initialized");
        assertEquals(contactId, lead.getOtherDetails().getPrimaryContactId(),
                "Primary contact should fall back to last contact when no decision maker");
    }

    // ==================== unsetOtherDecisionMakers – additional coverage ====================

    @Test
    void createContact_withDecisionMakerAndNonDecisionMakerSiblings_onlyUnsetsDecisionMakers() {
        // Arrange
        Long existingContactId1 = 20L;
        Contact nonDM = new Contact();
        nonDM.setId(existingContactId1);
        nonDM.setPersonId(200L);
        nonDM.setIsDecisionMaker(false);

        Long existingContactId2 = 30L;
        Contact isDM = new Contact();
        isDM.setId(existingContactId2);
        isDM.setPersonId(300L);
        isDM.setIsDecisionMaker(true);

        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(true).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>(List.of(existingContactId1, existingContactId2)));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.findByIdWithException(existingContactId1)).thenReturn(nonDM);
        when(contactRepositoryWrapper.findByIdWithException(existingContactId2)).thenReturn(isDM);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            if (c.getId() == null) c.setId(contactId);
            return c;
        });
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertFalse(isDM.getIsDecisionMaker(),
                "Existing decision maker should be unset");
        assertFalse(nonDM.getIsDecisionMaker(),
                "Non-decision maker should remain unchanged");
    }

    // ==================== createContact() – existing co-applicants list ====================

    @Test
    void createContact_withExistingCoApplicantsList_addsToExistingList() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Jane").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest request = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.CO_APPLICANT)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        lead.setContacts(new ArrayList<>());
        lead.setCoApplicants(new ArrayList<>(List.of(40L)));

        ReferralCodeRegistryResponse referralResponse = new ReferralCodeRegistryResponse();
        referralResponse.setReferralCode("REF-020");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            return c;
        });
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.APPLICANT), any(UUID.class)))
                .thenReturn(referralResponse);
        when(applicantRepositoryWrapper.saveWithException(any(Applicant.class))).thenAnswer(inv -> {
            Applicant a = inv.getArgument(0);
            a.setId(70L);
            return a;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadContactWriteService.createContact(leadIdentifier, request);

        // Assert
        assertEquals(2, lead.getCoApplicants().size(),
                "New co-applicant should be added to existing list");
    }

    // ==================== bulkUpdateContacts() Tests ====================

    @Test
    void bulkUpdateContacts_withCreates_createsContactsAndReturnsResponse() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest createRequest = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .creates(List.of(createRequest)).build();

        lead.setContacts(new ArrayList<>());

        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .identifier(contactIdentifier)
                .applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            c.setIdentifier(contactIdentifier);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadContactReadService.getContactById(eq(leadIdentifier), any(UUID.class))).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(any(UUID.class))).thenReturn(List.of());
        when(leadContactReadService.getIdentifiers(eq(leadIdentifier), any(UUID.class))).thenReturn(List.of());

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed(), "Total processed should be 1");
        assertEquals(1, result.getCreatedContacts().size(), "Should have 1 created contact");
        assertEquals(0, result.getDeletedCount(), "No deletes should be processed");
    }

    @Test
    void bulkUpdateContacts_withUpdates_updatesContactsAndReturnsResponse() {
        // Arrange
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Updated").lastName("Name").build();
        UpdateLeadContactRequest updateData = UpdateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(true).build();

        BulkContactsUpdateRequest.ContactUpdateItem updateItem = BulkContactsUpdateRequest.ContactUpdateItem.builder()
                .contactIdentifier(contactIdentifier.toString()).data(updateData).build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .updates(List.of(updateItem)).build();

        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .identifier(contactIdentifier).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(true).build();

        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadContactReadService.getContactById(eq(leadIdentifier), eq(contactIdentifier))).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(eq(contactIdentifier))).thenReturn(null);
        when(leadContactReadService.getIdentifiers(eq(leadIdentifier), eq(contactIdentifier))).thenReturn(null);

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed(), "Total processed should be 1");
        assertEquals(1, result.getUpdatedContacts().size(), "Should have 1 updated contact");
    }

    @Test
    void bulkUpdateContacts_withDeletes_deletesContactsAndReturnsCount() {
        // Arrange
        Long contactId2 = 20L;
        Contact contact2 = new Contact();
        contact2.setId(contactId2);
        contact2.setIdentifier(UUID.randomUUID());
        contact2.setPersonId(200L);
        contact2.setIsDecisionMaker(false);

        lead.setContacts(new ArrayList<>(List.of(contactId, contactId2)));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .deletes(List.of(contactIdentifier.toString())).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId2)).thenReturn(contact2);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed(), "Total processed should be 1");
        assertEquals(1, result.getDeletedCount(), "Should have 1 delete");
    }

    @Test
    void bulkUpdateContacts_withNullLists_returnsEmptyResponse() {
        // Arrange
        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .creates(null).updates(null).deletes(null)
                .addressOperations(null).identifierOperations(null).build();

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(0, result.getTotalProcessed(), "Nothing should be processed");
        assertTrue(result.getCreatedContacts().isEmpty(), "No created contacts");
        assertTrue(result.getUpdatedContacts().isEmpty(), "No updated contacts");
        assertEquals(0, result.getDeletedCount(), "No deletes");
    }

    @Test
    void bulkUpdateContacts_withAddressCreateOperation_createsAddress() {
        // Arrange
        AddressRequest addrReq = new AddressRequest();
        addrReq.setAddressType(AddressType.CURRENT);

        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(addrReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personWriteService.addAddress(eq(personId), any(AddressRequest.class))).thenReturn("addr-1");
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed(), "Should process 1 address operation");
        verify(personWriteService).addAddress(eq(personId), any(AddressRequest.class));
    }

    @Test
    void bulkUpdateContacts_withAddressUpdateOperation_updatesAddress() {
        // Arrange
        AddressRequest addrReq = new AddressRequest();
        addrReq.setAddressType(AddressType.CURRENT);

        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("update")
                .addressId("addr-1")
                .data(addrReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed());
        verify(personWriteService).updateAddress(eq(personId), eq("addr-1"), any(AddressRequest.class));
    }

    @Test
    void bulkUpdateContacts_withAddressDeleteOperation_throwsUnsupported() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("delete")
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Delete address operation should not be supported");
    }

    @Test
    void bulkUpdateContacts_withInvalidAddressOperation_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("INVALID_OP")
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw for invalid operation");
    }

    @Test
    void bulkUpdateContacts_withIdentifierCreateOperation_createsIdentifier() {
        // Arrange
        IdentifierRequest idReq = new IdentifierRequest();
        idReq.setType(IdentifierType.PAN);

        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(idReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(List.of());
        when(personWriteService.addIdentifier(eq(personId), any(IdentifierRequest.class)))
                .thenReturn(IdentifierData.builder().build());

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed());
        verify(personWriteService).addIdentifier(eq(personId), any(IdentifierRequest.class));
    }

    @Test
    void bulkUpdateContacts_withIdentifierUpdateOperation_updatesIdentifier() {
        // Arrange
        UUID identifierId = UUID.randomUUID();
        IdentifierRequest idReq = new IdentifierRequest();
        idReq.setType(IdentifierType.AADHAAR);

        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("update")
                .identifierId(identifierId.toString())
                .data(idReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(List.of());

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed());
        verify(personWriteService).updateIdentifier(eq(personId), eq(identifierId), any(IdentifierRequest.class));
    }

    @Test
    void bulkUpdateContacts_withIdentifierDeleteOperation_deletesIdentifier() {
        // Arrange
        UUID identifierId = UUID.randomUUID();

        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("delete")
                .identifierId(identifierId.toString())
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(1, result.getTotalProcessed());
        verify(personWriteService).deleteIdentifier(personId, identifierId);
    }

    @Test
    void bulkUpdateContacts_withInvalidIdentifierOperation_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("INVALID_OP")
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw for invalid identifier operation");
    }

    @Test
    void bulkUpdateContacts_withCreateReference_resolvesContactIdentifier() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").mobileNumbers(List.of(mobile)).build();
        CreateLeadContactRequest createRequest = CreateLeadContactRequest.builder()
                .contactPersonDetails(personDetails).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        AddressRequest addrReq = new AddressRequest();
        addrReq.setAddressType(AddressType.CURRENT);

        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier("create:0")
                .operation("create")
                .data(addrReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .creates(List.of(createRequest))
                .addressOperations(List.of(addressOp)).build();

        lead.setContacts(new ArrayList<>());

        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .identifier(contactIdentifier).applicantType(LeadContactPersonType.NONE)
                .isDecisionMaker(false).isPropertyOwner(false).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(personId).build());
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(contactId);
            c.setIdentifier(contactIdentifier);
            return c;
        });
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadContactReadService.getContactById(eq(leadIdentifier), any(UUID.class))).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(any(UUID.class))).thenReturn(List.of());
        when(leadContactReadService.getIdentifiers(eq(leadIdentifier), any(UUID.class))).thenReturn(List.of());
        when(personWriteService.addAddress(eq(personId), any(AddressRequest.class))).thenReturn("addr-1");

        // Act
        BulkContactsUpdateResponse result = leadContactWriteService.bulkUpdateContacts(leadIdentifier, request);

        // Assert
        assertEquals(2, result.getTotalProcessed(), "Should process create + address operation");
        verify(personWriteService).addAddress(eq(personId), any(AddressRequest.class));
    }

    @Test
    void bulkUpdateContacts_withNullContactIdentifier_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(null)
                .operation("create")
                .data(new AddressRequest())
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when contact identifier is null");
    }

    @Test
    void bulkUpdateContacts_withInvalidCreateReferenceFormat_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier("create:abc")
                .operation("create")
                .data(new AddressRequest())
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when create reference format is invalid");
    }

    @Test
    void bulkUpdateContacts_withInvalidUUID_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier("not-a-valid-uuid")
                .operation("create")
                .data(new AddressRequest())
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when contact identifier is not a valid UUID");
    }

    @Test
    void bulkUpdateContacts_withMissingAddressData_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(null)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when address data is null for create");
    }

    @Test
    void bulkUpdateContacts_withMissingAddressType_throwsValidationException() {
        // Arrange
        AddressRequest addrReq = new AddressRequest();
        addrReq.setAddressType(null);

        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(addrReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when address type is null for create");
    }

    @Test
    void bulkUpdateContacts_withDuplicateAddressType_throwsValidationException() {
        // Arrange
        AddressRequest addrReq = new AddressRequest();
        addrReq.setAddressType(AddressType.CURRENT);

        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(addrReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        AddressData existingAddr = new AddressData();
        existingAddr.setAddressType(AddressType.CURRENT);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of(existingAddr));

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when duplicate address type exists");
    }

    @Test
    void bulkUpdateContacts_withDuplicateIdentifierType_throwsValidationException() {
        // Arrange
        IdentifierRequest idReq = new IdentifierRequest();
        idReq.setType(IdentifierType.PAN);

        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(idReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        IdentifierData existingId = IdentifierData.builder().type(IdentifierType.PAN).build();
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(List.of(existingId));

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when duplicate identifier type exists");
    }

    @Test
    void bulkUpdateContacts_withMissingIdentifierData_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(null)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when identifier data is null for create");
    }

    @Test
    void bulkUpdateContacts_withMissingIdentifierType_throwsValidationException() {
        // Arrange
        IdentifierRequest idReq = new IdentifierRequest();
        idReq.setType(null);

        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("create")
                .data(idReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when identifier type is null for create");
    }

    @Test
    void bulkUpdateContacts_withMissingUpdateIdentifierId_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("update")
                .identifierId(null)
                .data(null)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when identifier ID is null for update");
    }

    @Test
    void bulkUpdateContacts_withMissingDeleteIdentifierId_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("delete")
                .identifierId(null)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when identifier ID is null for delete");
    }

    @Test
    void bulkUpdateContacts_withAddressUpdateMissingIdAndData_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("update")
                .addressId(null)
                .data(null)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when address ID and data are null for update");
    }

    @Test
    void bulkUpdateContacts_withNonExistentCreateReference_throwsValidationException() {
        // Arrange
        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier("create:99")
                .operation("create")
                .data(new AddressRequest())
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when create reference index doesn't exist");
    }

    @Test
    void bulkUpdateContacts_withMissingUpdateAddressType_throwsValidationException() {
        // Arrange
        AddressRequest addrReq = new AddressRequest();
        addrReq.setAddressType(null);

        BulkContactsUpdateRequest.AddressOperation addressOp = BulkContactsUpdateRequest.AddressOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("update")
                .addressId("addr-1")
                .data(addrReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .addressOperations(List.of(addressOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when address type is null for update");
    }

    @Test
    void bulkUpdateContacts_withMissingUpdateIdentifierType_throwsValidationException() {
        // Arrange
        IdentifierRequest idReq = new IdentifierRequest();
        idReq.setType(null);

        BulkContactsUpdateRequest.IdentifierOperation idOp = BulkContactsUpdateRequest.IdentifierOperation.builder()
                .contactIdentifier(contactIdentifier.toString())
                .operation("update")
                .identifierId(UUID.randomUUID().toString())
                .data(idReq)
                .build();

        BulkContactsUpdateRequest request = BulkContactsUpdateRequest.builder()
                .identifierOperations(List.of(idOp)).build();

        // Act & Assert
        assertThrows(LeadContactValidationException.class,
                () -> leadContactWriteService.bulkUpdateContacts(leadIdentifier, request),
                "Should throw when identifier type is null for update");
    }
}
