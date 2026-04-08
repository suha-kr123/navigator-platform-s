package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.consent.dto.ConsentResponse;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.consent.service.ConsentReadService;
import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import com.nivasafinance.features.lead.dto.EnquiryDetailsResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.service.PersonReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadCreditBureauReadServiceImplTest {

    @Mock
    private CreditBureauReadService creditBureauReadService;

    @Mock
    private ConsentReadService consentReadService;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private PersonReadService personReadService;

    @InjectMocks
    private LeadCreditBureauReadServiceImpl leadCreditBureauReadService;

    private UUID leadIdentifier;
    private UUID contactIdentifier;
    private UUID enquiryIdentifier;
    private Long leadId;
    private Long contactId;
    private Long personId;
    private Long enquiryId;
    private Lead lead;
    private Contact contact;
    private CreditBureauEnquiry enquiry;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        contactIdentifier = UUID.randomUUID();
        enquiryIdentifier = UUID.randomUUID();
        leadId = 1L;
        contactId = 10L;
        personId = 100L;
        enquiryId = 200L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setContacts(List.of(contactId));

        contact = new Contact();
        contact.setId(contactId);
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);
        contact.setCbEnquiryId(List.of(enquiryId));

        enquiry = new CreditBureauEnquiry();
        enquiry.setId(enquiryId);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.SUCCESS);
        enquiry.setConsentId(300L);
    }

    // ==================== getEnquiryDetailsForContact() Tests ====================

    @Test
    void getEnquiryDetailsForContact_withValidData_returnsEnquiryDetails() {
        // Arrange
        Person.CreditBureauDetails cbDetails = new Person.CreditBureauDetails();
        cbDetails.setLatestEnquiryId(enquiryId);
        PersonResponse personResponse = PersonResponse.builder().id(personId).cbDetails(cbDetails).build();
        ConsentResponse consent = ConsentResponse.builder().status(ConsentStatus.RECEIVED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(creditBureauReadService.getCbEnquiryEntityById(enquiryId)).thenReturn(enquiry);
        when(consentReadService.findById(300L)).thenReturn(Optional.of(consent));

        // Act
        Optional<EnquiryDetailsResponse> result = leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return enquiry details when data is valid");
        assertEquals(enquiryIdentifier, result.get().getEnquiryIdentifier(), "Enquiry identifier should match");
        assertEquals(CreditBureauEnquiryStatus.SUCCESS, result.get().getEnquiryStatus(), "Enquiry status should match");
        assertEquals(ConsentStatus.RECEIVED, result.get().getConsentStatus(), "Consent status should be mapped");
    }

    @Test
    void getEnquiryDetailsForContact_withNullCbDetails_returnsEmpty() {
        // Arrange
        PersonResponse personResponse = PersonResponse.builder().id(personId).cbDetails(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        Optional<EnquiryDetailsResponse> result = leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when person has no CB details");
    }

    @Test
    void getEnquiryDetailsForContact_withNullLatestEnquiryId_returnsEmpty() {
        // Arrange
        Person.CreditBureauDetails cbDetails = new Person.CreditBureauDetails();
        cbDetails.setLatestEnquiryId(null);
        PersonResponse personResponse = PersonResponse.builder().id(personId).cbDetails(cbDetails).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        Optional<EnquiryDetailsResponse> result = leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when latestEnquiryId is null");
    }

    @Test
    void getEnquiryDetailsForContact_whenEnquiryNotInContactList_returnsEmpty() {
        // Arrange
        Person.CreditBureauDetails cbDetails = new Person.CreditBureauDetails();
        cbDetails.setLatestEnquiryId(999L);
        PersonResponse personResponse = PersonResponse.builder().id(personId).cbDetails(cbDetails).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        Optional<EnquiryDetailsResponse> result = leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when latestEnquiryId not in contact's cbEnquiryId list");
    }

    @Test
    void getEnquiryDetailsForContact_withNullConsentId_returnsNullConsentStatus() {
        // Arrange
        enquiry.setConsentId(null);
        Person.CreditBureauDetails cbDetails = new Person.CreditBureauDetails();
        cbDetails.setLatestEnquiryId(enquiryId);
        PersonResponse personResponse = PersonResponse.builder().id(personId).cbDetails(cbDetails).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(creditBureauReadService.getCbEnquiryEntityById(enquiryId)).thenReturn(enquiry);

        // Act
        Optional<EnquiryDetailsResponse> result = leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return response even without consent");
        assertNull(result.get().getConsentStatus(), "Consent status should be null when consentId is null");
    }

    @Test
    void getEnquiryDetailsForContact_whenContactNotBelongsToLead_throwsBadRequestException() {
        // Arrange
        lead.setContacts(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier),
                "Should throw when contact does not belong to lead");
    }

    // ==================== getCustomerEnquiryByEnquiryIdentifier() Tests ====================

    @Test
    void getCustomerEnquiryByEnquiryIdentifier_withValidData_returnsResults() {
        // Arrange
        List<CustomerEnquiryResponse> expected = List.of(new CustomerEnquiryResponse());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(creditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(enquiryIdentifier)).thenReturn(expected);

        // Act
        List<CustomerEnquiryResponse> result = leadCreditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return customer enquiry results");
    }

    @Test
    void getCustomerEnquiryByEnquiryIdentifier_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    @Test
    void getCustomerEnquiryByEnquiryIdentifier_withNullCbEnquiryIdList_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when contact has null cbEnquiryId list");
    }

    // ==================== getSummaryResponseByEnquiryIdentifier() Tests ====================

    @Test
    void getSummaryResponseByEnquiryIdentifier_withValidData_returnsSummary() {
        // Arrange
        SummaryResponse summary = new SummaryResponse();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(creditBureauReadService.getSummaryByEnquiryIdentifier(enquiryIdentifier)).thenReturn(Optional.of(summary));

        // Act
        Optional<SummaryResponse> result = leadCreditBureauReadService.getSummaryResponseByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return summary when data is valid");
    }

    @Test
    void getSummaryResponseByEnquiryIdentifier_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getSummaryResponseByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== getEnquiryStatusByEnquiryIdentifier() Tests ====================

    @Test
    void getEnquiryStatusByEnquiryIdentifier_withValidData_returnsStatus() {
        // Arrange
        EnquiryStatusResponse statusResponse = new EnquiryStatusResponse();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(creditBureauReadService.getEnquiryStatusByEnquiryIdentifier(enquiryIdentifier)).thenReturn(Optional.of(statusResponse));

        // Act
        Optional<EnquiryStatusResponse> result = leadCreditBureauReadService.getEnquiryStatusByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return enquiry status when data is valid");
    }

    @Test
    void getEnquiryStatusByEnquiryIdentifier_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getEnquiryStatusByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== getConsentStatusByEnquiryIdentifier() Tests ====================

    @Test
    void getConsentStatusByEnquiryIdentifier_withValidConsent_returnsMappedResponse() {
        // Arrange
        UUID consentIdentifier = UUID.randomUUID();
        LocalDateTime sentTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime receivedTime = LocalDateTime.of(2025, 1, 1, 11, 0);
        ConsentResponse consent = ConsentResponse.builder()
                .identifier(consentIdentifier).status(ConsentStatus.RECEIVED)
                .consentSentTime(sentTime).consentReceivedTime(receivedTime).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(consentReadService.findById(300L)).thenReturn(Optional.of(consent));

        // Act
        Optional<EnquiryConsentStatusResponse> result = leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return consent status response");
        assertEquals(consentIdentifier, result.get().getConsentIdentifier(), "Consent identifier should be mapped");
        assertEquals(ConsentStatus.RECEIVED, result.get().getConsentStatus(), "Consent status should be mapped");
        assertEquals(sentTime, result.get().getConsentSentTime(), "Consent sent time should be mapped");
        assertEquals(receivedTime, result.get().getConsentReceivedTime(), "Consent received time should be mapped");
    }

    @Test
    void getConsentStatusByEnquiryIdentifier_withNullConsentId_returnsResponseWithoutConsentDetails() {
        // Arrange
        enquiry.setConsentId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act
        Optional<EnquiryConsentStatusResponse> result = leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return response even without consent");
        assertEquals(enquiryIdentifier, result.get().getEnquiryIdentifier(), "Enquiry identifier should be set");
        assertNull(result.get().getConsentIdentifier(), "Consent identifier should be null");
    }

    @Test
    void getConsentStatusByEnquiryIdentifier_whenConsentNotFound_returnsResponseWithoutConsentDetails() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(consentReadService.findById(300L)).thenReturn(Optional.empty());

        // Act
        Optional<EnquiryConsentStatusResponse> result = leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertTrue(result.isPresent(), "Should return response even when consent not found");
        assertEquals(enquiryIdentifier, result.get().getEnquiryIdentifier(), "Enquiry identifier should still be set");
    }

    @Test
    void getConsentStatusByEnquiryIdentifier_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== getScoreTrendsByEnquiryIdentifier() Tests ====================

    @Test
    void getScoreTrendsByEnquiryIdentifier_withValidData_returnsTrends() {
        // Arrange
        List<TrendsResponse> expected = List.of(new TrendsResponse());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(creditBureauReadService.getTrendsByEnquiryIdentifier(enquiryIdentifier)).thenReturn(expected);

        // Act
        List<TrendsResponse> result = leadCreditBureauReadService.getScoreTrendsByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return trends from credit bureau service");
    }

    @Test
    void getScoreTrendsByEnquiryIdentifier_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getScoreTrendsByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== getDemographicVariationsByEnquiryIdentifier() Tests ====================

    @Test
    void getDemographicVariationsByEnquiryIdentifier_withValidData_returnsVariations() {
        // Arrange
        List<DemographicVariationResponse> expected = List.of(new DemographicVariationResponse());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(creditBureauReadService.getDemographicVariationsByEnquiryIdentifier(enquiryIdentifier)).thenReturn(expected);

        // Act
        List<DemographicVariationResponse> result = leadCreditBureauReadService.getDemographicVariationsByEnquiryIdentifier(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertEquals(1, result.size(), "Should return demographic variations from credit bureau service");
    }

    @Test
    void getDemographicVariationsByEnquiryIdentifier_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getDemographicVariationsByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== getEnquiryIdForCbReportRegenerate() Tests ====================

    @Test
    void getEnquiryIdForCbReportRegenerate_withValidData_returnsEnquiryId() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act
        Long result = leadCreditBureauReadService.getEnquiryIdForCbReportRegenerate(
                leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        assertEquals(enquiryId, result, "Should return enquiry ID from the entity");
    }

    @Test
    void getEnquiryIdForCbReportRegenerate_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getEnquiryIdForCbReportRegenerate(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== validateContactBelongsToLead() edge cases ====================

    @Test
    void getCustomerEnquiryByEnquiryIdentifier_whenContactNotOnLead_throwsBadRequestException() {
        // Arrange
        lead.setContacts(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(
                        leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should throw when contact is not in lead's contacts list");
    }

    @Test
    void getEnquiryDetailsForContact_withEmptyContactsList_throwsBadRequestException() {
        // Arrange
        lead.setContacts(List.of());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier),
                "Should throw when lead has empty contacts list");
    }
}
