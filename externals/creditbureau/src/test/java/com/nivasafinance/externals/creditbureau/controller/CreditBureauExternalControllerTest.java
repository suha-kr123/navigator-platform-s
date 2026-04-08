package com.nivasafinance.externals.creditbureau.controller;

import com.nivasafinance.externals.creditbureau.dto.ContactCreditBureauResponse;
import com.nivasafinance.externals.creditbureau.service.CreditBureauExternalService;
import com.nivasafinance.features.consent.dto.AcceptConsentResponse;
import com.nivasafinance.features.consent.dto.WithdrawConsentResponse;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauExternalControllerTest {

    @Mock
    private CreditBureauExternalService creditBureauExternalService;

    @InjectMocks
    private CreditBureauExternalController controller;

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

    // ── getContact ──

    @Test
    void getContact_returnsOkWithContactResponse() {
        ContactCreditBureauResponse expectedResponse = ContactCreditBureauResponse.builder()
                .firstName("John").lastName("Doe")
                .mobileNumberDetails(Collections.emptyList())
                .identifierData(Collections.emptyList())
                .address(Collections.emptyList()).build();
        when(creditBureauExternalService.getContactByLeadAndContact(leadIdentifier, contactIdentifier))
                .thenReturn(expectedResponse);

        ResponseEntity<ContactCreditBureauResponse> result = controller.getContact(leadIdentifier, contactIdentifier);

        assertEquals(HttpStatus.OK, result.getStatusCode(), "Get contact should return HTTP 200");
        assertEquals(expectedResponse, result.getBody(), "Response body should match service response");
    }

    // ── getConsentStatus ──

    @Test
    void getConsentStatus_whenPresent_returnsOk() {
        EnquiryConsentStatusResponse consentResponse = EnquiryConsentStatusResponse.builder()
                .enquiryIdentifier(enquiryIdentifier)
                .consentIdentifier(consentIdentifier)
                .consentStatus(ConsentStatus.SENT).build();
        when(creditBureauExternalService.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier))
                .thenReturn(Optional.of(consentResponse));

        ResponseEntity<EnquiryConsentStatusResponse> result =
                controller.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier);

        assertEquals(HttpStatus.OK, result.getStatusCode(), "Present consent status should return HTTP 200");
        assertEquals(consentResponse, result.getBody(), "Response body should match consent status from service");
    }

    @Test
    void getConsentStatus_whenEmpty_returnsNotFound() {
        when(creditBureauExternalService.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier))
                .thenReturn(Optional.empty());

        ResponseEntity<EnquiryConsentStatusResponse> result =
                controller.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(), "Missing consent status should return HTTP 404");
        assertNull(result.getBody(), "Response body should be null when consent status is not found");
    }

    // ── acceptConsent ──

    @Test
    void acceptConsent_returnsOkWithAcceptResponse() {
        AcceptConsentResponse expectedResponse = AcceptConsentResponse.builder()
                .consentStatus(ConsentStatus.RECEIVED).build();
        when(creditBureauExternalService.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier))
                .thenReturn(expectedResponse);

        ResponseEntity<AcceptConsentResponse> result =
                controller.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        assertEquals(HttpStatus.OK, result.getStatusCode(), "Accept consent should return HTTP 200");
        assertEquals(ConsentStatus.RECEIVED, result.getBody().getConsentStatus(),
                "Accepted consent status should be RECEIVED");
    }

    // ── withdrawConsent ──

    @Test
    void withdrawConsent_returnsOkWithWithdrawResponse() {
        WithdrawConsentResponse expectedResponse = WithdrawConsentResponse.builder()
                .consentStatus(ConsentStatus.REQUEST_FOR_WITHDRAWAL).build();
        when(creditBureauExternalService.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier))
                .thenReturn(expectedResponse);

        ResponseEntity<WithdrawConsentResponse> result =
                controller.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);

        assertEquals(HttpStatus.OK, result.getStatusCode(), "Withdraw consent should return HTTP 200");
        assertEquals(ConsentStatus.REQUEST_FOR_WITHDRAWAL, result.getBody().getConsentStatus(),
                "Withdrawn consent status should be REQUEST_FOR_WITHDRAWAL");
    }
}
