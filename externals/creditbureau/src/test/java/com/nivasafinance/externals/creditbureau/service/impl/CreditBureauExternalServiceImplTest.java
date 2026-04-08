package com.nivasafinance.externals.creditbureau.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.externals.creditbureau.dto.ContactCreditBureauResponse;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.consent.dto.AcceptConsentResponse;
import com.nivasafinance.features.consent.dto.WithdrawConsentResponse;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import com.nivasafinance.features.lead.dto.LeadContactPersonDetails;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadCreditBureauReadService;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauExternalServiceImplTest {

    @Mock
    private LeadContactReadService leadContactReadService;

    @Mock
    private LeadCreditBureauReadService leadCreditBureauReadService;

    @Mock
    private LeadCreditBureauWriteService leadCreditBureauWriteService;

    @InjectMocks
    private CreditBureauExternalServiceImpl service;

    private UUID leadIdentifier;
    private UUID contactIdentifier;
    private UUID enquiryIdentifier;
    private UUID consentIdentifier;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        contactIdentifier = UUID.randomUUID();
        enquiryIdentifier = UUID.randomUUID();
        consentIdentifier = UUID.randomUUID();
    }

    // ── getContactByLeadAndContact ──

    @Test
    void getContactByLeadAndContact_withAllFields_returnsBuiltResponse() {
        List<MobileNumberDetails> mobileNumbers = List.of(
                MobileNumberDetails.builder().number("9876543210").isPrimary(true).build()
        );
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("John").lastName("Doe").dateOfBirth(LocalDate.of(1990, 1, 15))
                .mobileNumbers(mobileNumbers).build();
        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .contactPersonDetails(personDetails).build();
        List<AddressData> addresses = List.of(
                AddressData.builder().pincode("400001").build()
        );
        List<IdentifierData> identifiers = List.of(
                IdentifierData.builder().type(IdentifierType.PAN).identifier("ABCDE1234F").build()
        );

        when(leadContactReadService.getContactById(leadIdentifier, contactIdentifier)).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(addresses);
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(identifiers);

        ContactCreditBureauResponse result = service.getContactByLeadAndContact(leadIdentifier, contactIdentifier);

        assertEquals("John", result.getFirstName(), "First name should match contact person details");
        assertEquals("Doe", result.getLastName(), "Last name should match contact person details");
        assertEquals(LocalDate.of(1990, 1, 15), result.getDateOfBirth(), "Date of birth should match contact person details");
        assertEquals(mobileNumbers, result.getMobileNumberDetails(), "Mobile numbers should match contact person details");
        assertEquals(addresses, result.getAddress(), "Addresses should match fetched address data");
        assertEquals(identifiers, result.getIdentifierData(), "Identifiers should match fetched identifier data");
    }

    @Test
    void getContactByLeadAndContact_withNullMobileNumbers_returnsEmptyList() {
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Jane").mobileNumbers(null).build();
        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .contactPersonDetails(personDetails).build();

        when(leadContactReadService.getContactById(leadIdentifier, contactIdentifier)).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(List.of());

        ContactCreditBureauResponse result = service.getContactByLeadAndContact(leadIdentifier, contactIdentifier);

        assertEquals(Collections.emptyList(), result.getMobileNumberDetails(),
                "Null mobile numbers should be replaced with empty list");
    }

    @Test
    void getContactByLeadAndContact_withNullIdentifiers_returnsEmptyList() {
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Jane").mobileNumbers(List.of()).build();
        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .contactPersonDetails(personDetails).build();

        when(leadContactReadService.getContactById(leadIdentifier, contactIdentifier)).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(null);

        ContactCreditBureauResponse result = service.getContactByLeadAndContact(leadIdentifier, contactIdentifier);

        assertEquals(Collections.emptyList(), result.getIdentifierData(),
                "Null identifiers should be replaced with empty list");
    }

    @Test
    void getContactByLeadAndContact_withNullAddresses_returnsEmptyList() {
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Jane").mobileNumbers(List.of()).build();
        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .contactPersonDetails(personDetails).build();

        when(leadContactReadService.getContactById(leadIdentifier, contactIdentifier)).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(null);
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(List.of());

        ContactCreditBureauResponse result = service.getContactByLeadAndContact(leadIdentifier, contactIdentifier);

        assertEquals(Collections.emptyList(), result.getAddress(),
                "Null addresses should be replaced with empty list");
    }

    @Test
    void getContactByLeadAndContact_withAllNullCollections_returnsAllEmptyLists() {
        LeadContactPersonDetails personDetails = LeadContactPersonDetails.builder()
                .firstName("Jane").mobileNumbers(null).build();
        LeadContactResponse contactResponse = LeadContactResponse.builder()
                .contactPersonDetails(personDetails).build();

        when(leadContactReadService.getContactById(leadIdentifier, contactIdentifier)).thenReturn(contactResponse);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(null);
        when(leadContactReadService.getIdentifiers(leadIdentifier, contactIdentifier)).thenReturn(null);

        ContactCreditBureauResponse result = service.getContactByLeadAndContact(leadIdentifier, contactIdentifier);

        assertEquals(Collections.emptyList(), result.getMobileNumberDetails(),
                "Null mobile numbers should be replaced with empty list");
        assertEquals(Collections.emptyList(), result.getIdentifierData(),
                "Null identifiers should be replaced with empty list");
        assertEquals(Collections.emptyList(), result.getAddress(),
                "Null addresses should be replaced with empty list");
    }

    // ── getConsentStatus ──

    @Test
    void getConsentStatus_whenPresent_returnsOptionalWithResponse() {
        EnquiryConsentStatusResponse consentStatusResponse = EnquiryConsentStatusResponse.builder()
                .enquiryIdentifier(enquiryIdentifier)
                .consentIdentifier(consentIdentifier)
                .consentStatus(ConsentStatus.SENT).build();
        when(leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier))
                .thenReturn(Optional.of(consentStatusResponse));

        Optional<EnquiryConsentStatusResponse> result = service.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier);

        assertTrue(result.isPresent(), "Consent status should be present when read service returns a value");
        assertEquals(ConsentStatus.SENT, result.get().getConsentStatus(),
                "Consent status should match the value returned by read service");
    }

    @Test
    void getConsentStatus_whenEmpty_returnsEmptyOptional() {
        when(leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier))
                .thenReturn(Optional.empty());

        Optional<EnquiryConsentStatusResponse> result = service.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier);

        assertTrue(result.isEmpty(), "Consent status should be empty when read service returns no value");
    }

    // ── acceptConsent ──

    @Test
    void acceptConsent_callsWriteServiceAndReturnsReceivedStatus() {
        AcceptConsentResponse result = service.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        verify(leadCreditBureauWriteService).acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        assertEquals(ConsentStatus.RECEIVED, result.getConsentStatus(),
                "Accept consent should return RECEIVED status");
    }

    // ── withdrawConsent ──

    @Test
    void withdrawConsent_callsWriteServiceAndReturnsRequestForWithdrawalStatus() {
        WithdrawConsentResponse result = service.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        verify(leadCreditBureauWriteService).withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        assertEquals(ConsentStatus.REQUEST_FOR_WITHDRAWAL, result.getConsentStatus(),
                "Withdraw consent should return REQUEST_FOR_WITHDRAWAL status");
    }
}
