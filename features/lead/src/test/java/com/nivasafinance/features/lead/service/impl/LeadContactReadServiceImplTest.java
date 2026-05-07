package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.RelatedContactResponse;
import com.nivasafinance.features.lead.entity.Applicant;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.ContactRelation;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.ContactRelationType;
import com.nivasafinance.features.lead.enums.LeadContactPersonType;
import com.nivasafinance.features.lead.exception.ContactNotFoundException;
import com.nivasafinance.features.lead.repository.ApplicantRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRelationRepositoryWrapper;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.enums.Gender;
import com.nivasafinance.features.person.service.PersonReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadContactReadServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private ApplicantRepositoryWrapper applicantRepositoryWrapper;

    @Mock
    private ContactRelationRepositoryWrapper contactRelationRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private PersonReadService personReadService;

    @InjectMocks
    private LeadContactReadServiceImpl leadContactReadService;

    private UUID leadIdentifier;
    private Long leadId;
    private Lead lead;
    private Contact contact;
    private UUID contactIdentifier;
    private Long contactId;
    private Long personId;
    private PersonResponse personResponse;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        leadId = 1L;
        contactIdentifier = UUID.randomUUID();
        contactId = 10L;
        personId = 100L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);

        contact = new Contact();
        contact.setId(contactId);
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);
        contact.setIsDecisionMaker(false);
        contact.setIsPropertyOwner(false);

        personResponse = PersonResponse.builder()
                .id(personId)
                .firstName("John")
                .middleName("M")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();
    }

    // ==================== getContacts() Tests ====================

    @Test
    void getContacts_withNullContacts_returnsEmptyList() {
        // Arrange
        lead.setContacts(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertNotNull(result, "Result list should never be null");
        assertTrue(result.isEmpty(), "Should return empty list when lead has no contacts");
    }

    @Test
    void getContacts_withContacts_returnsMappedResponses() {
        // Arrange
        lead.setContacts(List.of(contactId));
        lead.setApplicant(null);
        lead.setCoApplicants(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return one contact response");
        assertEquals(contactIdentifier, result.get(0).getIdentifier(), "Contact identifier should match");
        assertEquals("John", result.get(0).getContactPersonDetails().getFirstName(), "First name should be mapped from person");
    }

    @Test
    void getContacts_withApplicantContact_returnsApplicantType() {
        // Arrange
        Long applicantId = 50L;
        Applicant applicant = new Applicant();
        applicant.setId(applicantId);
        applicant.setPersonId(personId);

        lead.setContacts(List.of(contactId));
        lead.setApplicant(applicantId);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(applicantRepositoryWrapper.findByIdWithException(applicantId)).thenReturn(applicant);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertEquals(LeadContactPersonType.APPLICANT, result.get(0).getApplicantType(),
                "Contact whose personId matches applicant should have APPLICANT type");
    }

    @Test
    void getContacts_withCoApplicantContact_returnsCoApplicantType() {
        // Arrange
        Long coApplicantId = 60L;
        Applicant coApplicant = new Applicant();
        coApplicant.setId(coApplicantId);
        coApplicant.setPersonId(personId);

        lead.setContacts(List.of(contactId));
        lead.setApplicant(null);
        lead.setCoApplicants(List.of(coApplicantId));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(applicantRepositoryWrapper.findByIdWithException(coApplicantId)).thenReturn(coApplicant);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertEquals(LeadContactPersonType.CO_APPLICANT, result.get(0).getApplicantType(),
                "Contact whose personId matches co-applicant should have CO_APPLICANT type");
    }

    @Test
    void getContacts_withNonApplicantContact_returnsNoneType() {
        // Arrange
        Long applicantId = 50L;
        Applicant applicant = new Applicant();
        applicant.setId(applicantId);
        applicant.setPersonId(999L);

        lead.setContacts(List.of(contactId));
        lead.setApplicant(applicantId);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(applicantRepositoryWrapper.findByIdWithException(applicantId)).thenReturn(applicant);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertEquals(LeadContactPersonType.NONE, result.get(0).getApplicantType(),
                "Contact not matching any applicant should have NONE type");
    }

    // ==================== getContactById() Tests ====================

    @Test
    void getContactById_withValidContact_returnsResponse() {
        // Arrange
        lead.setContacts(List.of(contactId));
        lead.setApplicant(null);
        lead.setCoApplicants(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        LeadContactResponse result = leadContactReadService.getContactById(leadIdentifier, contactIdentifier);

        // Assert
        assertNotNull(result, "Contact response should not be null");
        assertEquals(contactIdentifier, result.getIdentifier(), "Contact identifier should match");
        assertEquals("Doe", result.getContactPersonDetails().getLastName(), "Last name should be mapped from person");
    }

    @Test
    void getContactById_withNullContacts_throwsContactNotFoundException() {
        // Arrange
        lead.setContacts(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactReadService.getContactById(leadIdentifier, contactIdentifier),
                "Should throw ContactNotFoundException when lead has no contacts");
    }

    @Test
    void getContactById_withNonExistentContact_throwsContactNotFoundException() {
        // Arrange
        UUID unknownContactId = UUID.randomUUID();
        Contact otherContact = new Contact();
        otherContact.setId(contactId);
        otherContact.setIdentifier(UUID.randomUUID());

        lead.setContacts(List.of(contactId));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(otherContact);

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactReadService.getContactById(leadIdentifier, unknownContactId),
                "Should throw ContactNotFoundException when contact identifier does not match any contact");
    }

    // ==================== getAddresses() Tests ====================

    @Test
    void getAddresses_withValidContact_returnsAddresses() {
        // Arrange
        List<AddressData> addresses = List.of(AddressData.builder().build());
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getAddresses(personId)).thenReturn(addresses);

        // Act
        List<AddressData> result = leadContactReadService.getAddresses(contactIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return addresses from person service");
        verify(personReadService).getAddresses(personId);
    }

    // ==================== getAddress() Tests ====================

    @Test
    void getAddress_withValidAddress_returnsAddress() {
        // Arrange
        String addressId = "addr-1";
        AddressData addressData = AddressData.builder().build();
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getAddress(personId, addressId)).thenReturn(addressData);

        // Act
        AddressData result = leadContactReadService.getAddress(contactIdentifier, addressId);

        // Assert
        assertNotNull(result, "Should return address from person service");
    }

    @Test
    void getAddress_whenPersonServiceThrowsNotFound_throwsResponseStatusException() {
        // Arrange
        String addressId = "addr-missing";
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getAddress(personId, addressId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> leadContactReadService.getAddress(contactIdentifier, addressId),
                "Should re-throw as NOT_FOUND ResponseStatusException");
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(), "Status should be 404 NOT_FOUND");
    }

    // ==================== getIdentifiers() Tests ====================

    @Test
    void getIdentifiers_withValidContact_returnsIdentifiers() {
        // Arrange
        lead.setContacts(List.of(contactId));
        List<IdentifierData> identifiers = List.of(IdentifierData.builder().build());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getIdentifiers(personId)).thenReturn(identifiers);

        // Act
        List<IdentifierData> result = leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return identifiers from person service");
        verify(personReadService).getIdentifiers(personId);
    }

    @Test
    void getIdentifiers_withNonExistentContact_throwsContactNotFoundException() {
        // Arrange
        lead.setContacts(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier),
                "Should throw ContactNotFoundException when lead has no contacts");
    }

    // ==================== getIdentifier() Tests ====================

    @Test
    void getIdentifier_withValidIdentifier_returnsIdentifier() {
        // Arrange
        UUID identifierId = UUID.randomUUID();
        lead.setContacts(List.of(contactId));
        IdentifierData identifierData = IdentifierData.builder().build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getIdentifier(personId, identifierId)).thenReturn(identifierData);

        // Act
        IdentifierData result = leadContactReadService.getIdentifier(leadIdentifier, contactIdentifier, identifierId);

        // Assert
        assertNotNull(result, "Should return identifier from person service");
    }

    @Test
    void getIdentifier_whenPersonServiceThrowsNotFound_throwsResponseStatusException() {
        // Arrange
        UUID identifierId = UUID.randomUUID();
        lead.setContacts(List.of(contactId));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getIdentifier(personId, identifierId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> leadContactReadService.getIdentifier(leadIdentifier, contactIdentifier, identifierId),
                "Should re-throw as NOT_FOUND ResponseStatusException");
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode(), "Status should be 404 NOT_FOUND");
    }

    @Test
    void getIdentifier_withNonExistentContact_throwsContactNotFoundException() {
        // Arrange
        UUID identifierId = UUID.randomUUID();
        lead.setContacts(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ContactNotFoundException.class,
                () -> leadContactReadService.getIdentifier(leadIdentifier, contactIdentifier, identifierId),
                "Should throw ContactNotFoundException when lead has no contacts");
    }

    // ==================== getRelatedContacts() Tests ====================

    @Test
    void getRelatedContacts_withNoRelations_returnsEmptyList() {
        lead.setContacts(List.of(contactId));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRelationRepositoryWrapper.findAllByContactId(contactId)).thenReturn(List.of());

        List<RelatedContactResponse> result = leadContactReadService.getRelatedContacts(leadIdentifier, contactIdentifier);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when no relations exist");
    }

    @Test
    void getRelatedContacts_withRelation_returnsMappedResponse() {
        Long relatedContactId = 20L;
        UUID relatedContactIdentifier = UUID.randomUUID();
        Long relatedPersonId = 200L;

        Contact relatedContact = new Contact();
        relatedContact.setId(relatedContactId);
        relatedContact.setIdentifier(relatedContactIdentifier);
        relatedContact.setPersonId(relatedPersonId);
        relatedContact.setIsDecisionMaker(false);
        relatedContact.setIsPropertyOwner(false);

        PersonResponse relatedPersonResponse = PersonResponse.builder()
                .id(relatedPersonId)
                .firstName("Jane")
                .lastName("Doe")
                .build();

        ContactRelation relation = new ContactRelation();
        relation.setContactId(contactId);
        relation.setRelatedContactId(relatedContactId);
        relation.setRelation(ContactRelationType.SPOUSE);

        lead.setContacts(List.of(contactId));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRelationRepositoryWrapper.findAllByContactId(contactId)).thenReturn(List.of(relation));
        when(contactRepositoryWrapper.findByIdWithException(relatedContactId)).thenReturn(relatedContact);
        when(personReadService.getPersonById(relatedPersonId)).thenReturn(relatedPersonResponse);

        List<RelatedContactResponse> result = leadContactReadService.getRelatedContacts(leadIdentifier, contactIdentifier);

        assertEquals(1, result.size());
        assertEquals(relatedContactIdentifier, result.get(0).getIdentifier());
        assertEquals(ContactRelationType.SPOUSE, result.get(0).getRelation());
        assertEquals("Jane", result.get(0).getContactPersonDetails().getFirstName());
    }

    @Test
    void getRelatedContacts_withMultipleRelations_returnsAllMapped() {
        Long relatedContactId1 = 20L;
        Long relatedContactId2 = 30L;

        Contact relatedContact1 = new Contact();
        relatedContact1.setId(relatedContactId1);
        relatedContact1.setIdentifier(UUID.randomUUID());
        relatedContact1.setPersonId(200L);
        relatedContact1.setIsDecisionMaker(false);
        relatedContact1.setIsPropertyOwner(false);

        Contact relatedContact2 = new Contact();
        relatedContact2.setId(relatedContactId2);
        relatedContact2.setIdentifier(UUID.randomUUID());
        relatedContact2.setPersonId(300L);
        relatedContact2.setIsDecisionMaker(false);
        relatedContact2.setIsPropertyOwner(false);

        ContactRelation relation1 = new ContactRelation();
        relation1.setContactId(contactId);
        relation1.setRelatedContactId(relatedContactId1);
        relation1.setRelation(ContactRelationType.FATHER);

        ContactRelation relation2 = new ContactRelation();
        relation2.setContactId(contactId);
        relation2.setRelatedContactId(relatedContactId2);
        relation2.setRelation(ContactRelationType.MOTHER);

        lead.setContacts(List.of(contactId));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRelationRepositoryWrapper.findAllByContactId(contactId)).thenReturn(List.of(relation1, relation2));
        when(contactRepositoryWrapper.findByIdWithException(relatedContactId1)).thenReturn(relatedContact1);
        when(contactRepositoryWrapper.findByIdWithException(relatedContactId2)).thenReturn(relatedContact2);
        when(personReadService.getPersonById(200L)).thenReturn(PersonResponse.builder().id(200L).firstName("Father").build());
        when(personReadService.getPersonById(300L)).thenReturn(PersonResponse.builder().id(300L).firstName("Mother").build());

        List<RelatedContactResponse> result = leadContactReadService.getRelatedContacts(leadIdentifier, contactIdentifier);

        assertEquals(2, result.size());
        assertEquals(ContactRelationType.FATHER, result.get(0).getRelation());
        assertEquals(ContactRelationType.MOTHER, result.get(1).getRelation());
    }

    @Test
    void getRelatedContacts_withContactNotInLead_throwsContactNotFoundException() {
        lead.setContacts(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        assertThrows(ContactNotFoundException.class,
                () -> leadContactReadService.getRelatedContacts(leadIdentifier, contactIdentifier),
                "Should throw ContactNotFoundException when contact does not belong to lead");
    }

    // ==================== getContacts() - mapToContactResponse mapping Tests ====================

    @Test
    void getContacts_mapsDecisionMakerAndPropertyOwnerFlags() {
        // Arrange
        contact.setIsDecisionMaker(true);
        contact.setIsPropertyOwner(true);
        lead.setContacts(List.of(contactId));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertTrue(result.get(0).getIsDecisionMaker(), "Decision maker flag should be true");
        assertTrue(result.get(0).getIsPropertyOwner(), "Property owner flag should be true");
    }

    @Test
    void getContacts_withMultipleContacts_returnsAllMapped() {
        // Arrange
        Long contactId2 = 20L;
        UUID contactIdentifier2 = UUID.randomUUID();
        Long personId2 = 200L;

        Contact contact2 = new Contact();
        contact2.setId(contactId2);
        contact2.setIdentifier(contactIdentifier2);
        contact2.setPersonId(personId2);
        contact2.setIsDecisionMaker(false);
        contact2.setIsPropertyOwner(false);

        PersonResponse personResponse2 = PersonResponse.builder()
                .id(personId2)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        lead.setContacts(List.of(contactId, contactId2));
        lead.setApplicant(null);
        lead.setCoApplicants(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdWithException(contactId2)).thenReturn(contact2);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getPersonById(personId2)).thenReturn(personResponse2);

        // Act
        List<LeadContactResponse> result = leadContactReadService.getContacts(leadIdentifier);

        // Assert
        assertEquals(2, result.size(), "Should return two contact responses");
        assertEquals("John", result.get(0).getContactPersonDetails().getFirstName(), "First contact first name should match");
        assertEquals("Jane", result.get(1).getContactPersonDetails().getFirstName(), "Second contact first name should match");
    }
}
