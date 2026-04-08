package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.analytics.AnalyticsHelper;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.consent.dto.ConsentResponse;
import com.nivasafinance.features.consent.service.ConsentReadService;
import com.nivasafinance.features.consent.service.ConsentWriteService;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.lead.dto.InitiateCbEnquiryResponse;
import com.nivasafinance.features.lead.dto.RecordCbConsentResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadCreditBureauReadService;
import com.nivasafinance.features.person.dto.CreditBureauEnquiryInitiationResult;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.RecordCbConsentResult;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonCreditBureauService;
import com.nivasafinance.features.person.service.PersonReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadCreditBureauWriteServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private PersonReadService personReadService;

    @Mock
    private PersonCreditBureauService personCreditBureauService;

    @Mock
    private ConsentReadService consentReadService;

    @Mock
    private ConsentWriteService consentWriteService;

    @Mock
    private CreditBureauReadService creditBureauReadService;

    @Mock
    private LeadCreditBureauReadService leadCreditBureauReadService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AnalyticsHelper analyticsHelper;

    @InjectMocks
    private LeadCreditBureauWriteServiceImpl leadCreditBureauWriteService;

    private UUID leadIdentifier;
    private UUID contactIdentifier;
    private UUID enquiryIdentifier;
    private UUID consentIdentifier;
    private Long leadId;
    private Long contactId;
    private Long personId;
    private Long enquiryId;
    private Long consentId;
    private Lead lead;
    private Contact contact;
    private CreditBureauEnquiry enquiry;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        contactIdentifier = UUID.randomUUID();
        enquiryIdentifier = UUID.randomUUID();
        consentIdentifier = UUID.randomUUID();
        leadId = 1L;
        contactId = 10L;
        personId = 100L;
        enquiryId = 200L;
        consentId = 300L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setContacts(List.of(contactId));

        contact = new Contact();
        contact.setId(contactId);
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);
        contact.setCbEnquiryId(new ArrayList<>(List.of(enquiryId)));

        enquiry = new CreditBureauEnquiry();
        enquiry.setId(enquiryId);
        enquiry.setIdentifier(enquiryIdentifier);
        enquiry.setStatus(CreditBureauEnquiryStatus.SUCCESS);
        enquiry.setConsentId(consentId);
    }

    // ==================== initiateEnquiry() Tests ====================

    @Test
    void initiateEnquiry_withValidData_returnsResponse() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).firstName("John").mobileNumbers(List.of(mobile)).build();
        List<AddressData> addresses = List.of(AddressData.builder().build());

        CreditBureauEnquiryResponse cbResponse = CreditBureauEnquiryResponse.builder()
                .id(enquiryId).identifier(enquiryIdentifier).status(CreditBureauEnquiryStatus.INITIATED)
                .consentIdentifier(consentIdentifier).build();
        CreditBureauEnquiryInitiationResult initiationResult = CreditBureauEnquiryInitiationResult.builder()
                .response(cbResponse).asyncPullFuture(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(addresses);
        when(personCreditBureauService.initiateCreditBureauEnquiry(any())).thenReturn(initiationResult);

        // Act
        InitiateCbEnquiryResponse result = leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(enquiryIdentifier, result.getEnquiryIdentifier(), "Enquiry identifier should match");
        assertEquals(CreditBureauEnquiryStatus.INITIATED, result.getStatus(), "Status should be INITIATED");
        assertEquals(consentIdentifier, result.getConsentIdentifier(), "Consent identifier should match");
    }

    @Test
    void initiateEnquiry_withNullStatus_defaultsToInitiated() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).firstName("John").mobileNumbers(List.of(mobile)).build();

        CreditBureauEnquiryResponse cbResponse = CreditBureauEnquiryResponse.builder()
                .id(enquiryId).identifier(enquiryIdentifier).status(null).build();
        CreditBureauEnquiryInitiationResult initiationResult = CreditBureauEnquiryInitiationResult.builder()
                .response(cbResponse).asyncPullFuture(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());
        when(personCreditBureauService.initiateCreditBureauEnquiry(any())).thenReturn(initiationResult);

        // Act
        InitiateCbEnquiryResponse result = leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier);

        // Assert
        assertEquals(CreditBureauEnquiryStatus.INITIATED, result.getStatus(),
                "Status should default to INITIATED when response status is null");
    }

    @Test
    void initiateEnquiry_whenContactNotOnLead_throwsBadRequestException() {
        // Arrange
        lead.setContacts(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier),
                "Should throw when contact does not belong to lead");
    }

    @Test
    void initiateEnquiry_withMissingFirstName_throwsBadRequestException() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).firstName(null).mobileNumbers(List.of(mobile)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier),
                "Should throw when person firstName is missing");
    }

    @Test
    void initiateEnquiry_withMissingMobileNumbers_throwsBadRequestException() {
        // Arrange
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).firstName("John").mobileNumbers(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier),
                "Should throw when person has no mobile numbers");
    }

    @Test
    void initiateEnquiry_withNoPersonAddresses_fallsBackToLeadPropertyAddress() {
        // Arrange
        AddressData propertyAddress = AddressData.builder().build();
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder().address(propertyAddress).build();
        lead.setOtherDetails(Lead.OtherDetails.builder().propertyDetails(propertyDetails).build());

        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).firstName("John").mobileNumbers(List.of(mobile)).build();

        CreditBureauEnquiryResponse cbResponse = CreditBureauEnquiryResponse.builder()
                .id(enquiryId).identifier(enquiryIdentifier).status(CreditBureauEnquiryStatus.INITIATED).build();
        CreditBureauEnquiryInitiationResult initiationResult = CreditBureauEnquiryInitiationResult.builder()
                .response(cbResponse).asyncPullFuture(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());
        when(personCreditBureauService.initiateCreditBureauEnquiry(any())).thenReturn(initiationResult);

        // Act
        InitiateCbEnquiryResponse result = leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier);

        // Assert
        assertNotNull(result, "Should succeed with fallback property address");
        verify(personCreditBureauService).initiateCreditBureauEnquiry(any());
    }

    @Test
    void initiateEnquiry_updatesContactCbEnquiryIdList() {
        // Arrange
        contact.setCbEnquiryId(null);

        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).firstName("John").mobileNumbers(List.of(mobile)).build();

        Long newEnquiryId = 500L;
        CreditBureauEnquiryResponse cbResponse = CreditBureauEnquiryResponse.builder()
                .id(newEnquiryId).identifier(enquiryIdentifier).status(CreditBureauEnquiryStatus.INITIATED).build();
        CreditBureauEnquiryInitiationResult initiationResult = CreditBureauEnquiryInitiationResult.builder()
                .response(cbResponse).asyncPullFuture(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());
        when(personCreditBureauService.initiateCreditBureauEnquiry(any())).thenReturn(initiationResult);
        when(contactRepositoryWrapper.saveWithException(any(Contact.class))).thenReturn(contact);

        // Act
        leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier);

        // Assert
        assertNotNull(contact.getCbEnquiryId(), "CbEnquiryId list should be initialized");
        assertTrue(contact.getCbEnquiryId().contains(newEnquiryId), "New enquiry ID should be added to contact");
        verify(contactRepositoryWrapper).saveWithException(contact);
    }

    // ==================== recordCbConsentReceived() Tests ====================

    @Test
    void recordCbConsentReceived_withValidData_returnsResponse() {
        // Arrange
        UUID resultConsentId = UUID.randomUUID();
        RecordCbConsentResult result = RecordCbConsentResult.builder().consentIdentifier(resultConsentId).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personCreditBureauService.recordCbConsentReceived(personId)).thenReturn(result);

        // Act
        RecordCbConsentResponse response = leadCreditBureauWriteService.recordCbConsentReceived(leadIdentifier, contactIdentifier);

        // Assert
        assertEquals(resultConsentId, response.getConsentIdentifier(), "Consent identifier should match");
        verify(analyticsHelper).captureLead(any());
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void recordCbConsentReceived_whenContactNotOnLead_throwsBadRequestException() {
        // Arrange
        lead.setContacts(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.recordCbConsentReceived(leadIdentifier, contactIdentifier),
                "Should throw when contact does not belong to lead");
    }

    // ==================== acceptConsent() Tests ====================

    @Test
    void acceptConsent_withValidData_callsConsentWriteService() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).mobileNumbers(List.of(mobile)).build();
        ConsentResponse consentResponse = ConsentResponse.builder().id(consentId).identifier(consentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(consentReadService.findByIdentifierWithException(consentIdentifier)).thenReturn(consentResponse);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());
        when(personCreditBureauService.onConsentGranted(eq(consentId), eq(enquiryIdentifier), any())).thenReturn(null);

        // Act
        leadCreditBureauWriteService.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        // Assert
        verify(consentWriteService).acceptConsent(any());
        verify(personCreditBureauService).onConsentGranted(eq(consentId), eq(enquiryIdentifier), any());
    }

    @Test
    void acceptConsent_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.acceptConsent(
                        leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    @Test
    void acceptConsent_withNullMobileNumbers_passesNullRecipientPhone() {
        // Arrange
        PersonResponse personResponse = PersonResponse.builder().id(personId).mobileNumbers(null).build();
        ConsentResponse consentResponse = ConsentResponse.builder().id(consentId).identifier(consentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(consentReadService.findByIdentifierWithException(consentIdentifier)).thenReturn(consentResponse);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(personReadService.getAddresses(personId)).thenReturn(Collections.emptyList());
        when(personCreditBureauService.onConsentGranted(eq(consentId), eq(enquiryIdentifier), any())).thenReturn(null);

        // Act
        leadCreditBureauWriteService.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        // Assert
        verify(consentWriteService).acceptConsent(any());
    }

    // ==================== resendConsent() Tests ====================

    @Test
    void resendConsent_withValidData_callsConsentWriteService() {
        // Arrange
        MobileNumberDetails mobile = MobileNumberDetails.builder().number("9876543210").isPrimary(true).build();
        PersonResponse personResponse = PersonResponse.builder()
                .id(personId).mobileNumbers(List.of(mobile)).build();
        ConsentResponse consentResponse = ConsentResponse.builder().id(consentId).identifier(consentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(consentReadService.findByIdentifierWithException(consentIdentifier)).thenReturn(consentResponse);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // Act
        leadCreditBureauWriteService.resendConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        // Assert
        verify(consentWriteService).resendConsent(any());
    }

    @Test
    void resendConsent_whenConsentDoesNotBelongToEnquiry_throwsBadRequestException() {
        // Arrange
        ConsentResponse consentResponse = ConsentResponse.builder().id(999L).identifier(consentIdentifier).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);
        when(consentReadService.findByIdentifierWithException(consentIdentifier)).thenReturn(consentResponse);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.resendConsent(
                        leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier),
                "Should throw when consent does not belong to the enquiry");
    }

    @Test
    void resendConsent_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.resendConsent(
                        leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    // ==================== withdrawConsent() Tests ====================

    @Test
    void withdrawConsent_withValidData_callsConsentWriteService() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act
        leadCreditBureauWriteService.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        // Assert
        verify(consentWriteService).withdrawConsent(any());
    }

    @Test
    void withdrawConsent_whenEnquiryNotInContact_throwsBadRequestException() {
        // Arrange
        contact.setCbEnquiryId(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier)).thenReturn(enquiry);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.withdrawConsent(
                        leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier),
                "Should throw when enquiry does not belong to contact");
    }

    @Test
    void withdrawConsent_whenContactNotOnLead_throwsBadRequestException() {
        // Arrange
        lead.setContacts(List.of(999L));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.withdrawConsent(
                        leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier),
                "Should throw when contact does not belong to lead");
    }

    // ==================== regenerateCbReport() Tests ====================

    @Test
    void regenerateCbReport_withValidData_publishesEvent() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadCreditBureauReadService.getEnquiryIdForCbReportRegenerate(leadIdentifier, contactIdentifier, enquiryIdentifier))
                .thenReturn(enquiryId);

        // Act
        leadCreditBureauWriteService.regenerateCbReport(leadIdentifier, contactIdentifier, enquiryIdentifier);

        // Assert
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void regenerateCbReport_whenReadServiceThrows_propagatesException() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadCreditBureauReadService.getEnquiryIdForCbReportRegenerate(leadIdentifier, contactIdentifier, enquiryIdentifier))
                .thenThrow(new BadRequestException("Enquiry does not belong to the provided contact"));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadCreditBureauWriteService.regenerateCbReport(leadIdentifier, contactIdentifier, enquiryIdentifier),
                "Should propagate exception from read service");
    }
}
