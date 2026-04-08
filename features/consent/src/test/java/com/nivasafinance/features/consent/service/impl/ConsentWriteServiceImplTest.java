package com.nivasafinance.features.consent.service.impl;

import com.nivasafinance.common.context.RequestContext;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.RequestMetadata;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.consent.dto.AcceptConsentRequest;
import com.nivasafinance.features.consent.dto.ConsentReceivedRequest;
import com.nivasafinance.features.consent.dto.CreateAndSendConsent;
import com.nivasafinance.features.consent.dto.ResendConsentRequest;
import com.nivasafinance.features.consent.dto.WithdrawConsentRequest;
import com.nivasafinance.features.consent.entity.Consent;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.consent.exception.ConsentOperationException;
import com.nivasafinance.features.consent.repository.ConsentRepositoryWrapper;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentWriteServiceImplTest {

    @Mock
    private ConsentRepositoryWrapper consentRepositoryWrapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ConsentWriteServiceImpl consentWriteService;

    private UUID consentIdentifier;
    private UUID enquiryIdentifier;
    private UUID leadIdentifier;
    private UUID contactIdentifier;

    private static final String CONSENT_URL_BASE = "https://consent.example.com";
    private static final String TEST_USERNAME = "test-user";
    private static final String TEST_PHONE = "9876543210";
    private static final Long TEST_PERSON_ID = 100L;
    private static final Long TEST_CONSENT_ID = 1L;

    @BeforeEach
    void setUp() {
        consentIdentifier = UUID.randomUUID();
        enquiryIdentifier = UUID.randomUUID();
        leadIdentifier = UUID.randomUUID();
        contactIdentifier = UUID.randomUUID();

        ReflectionTestUtils.setField(consentWriteService, "consentUrlBase", CONSENT_URL_BASE);
        UserContext.setUsername(TEST_USERNAME);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        RequestContext.clear();
    }

    // ==================== createAndSendConsent() Tests ====================

    @Test
    void createAndSendConsent_withValidCommand_savesConsentWithSentStatusAndPublishesEvent() {
        // Arrange
        CreateAndSendConsent command = CreateAndSendConsent.builder()
                .personId(TEST_PERSON_ID)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .recipientPhone(TEST_PHONE)
                .build();

        Consent savedConsent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.saveWithException(any(Consent.class))).thenReturn(savedConsent);

        // Act
        Consent result = consentWriteService.createAndSendConsent(command);

        // Assert
        assertNotNull(result, "Saved consent should not be null");
        assertEquals(ConsentStatus.SENT, result.getStatus(), "Consent should be saved with SENT status");

        ArgumentCaptor<Consent> consentCaptor = ArgumentCaptor.forClass(Consent.class);
        verify(consentRepositoryWrapper).saveWithException(consentCaptor.capture());
        Consent capturedConsent = consentCaptor.getValue();
        assertEquals(ConsentStatus.SENT, capturedConsent.getStatus(), "Consent passed to save should have SENT status");
        assertNotNull(capturedConsent.getConsentSentDetails(), "Sent details should be populated before save");
        assertNull(capturedConsent.getConsentReceivedDetails(), "Received details should be null on create");
        assertNull(capturedConsent.getConsentWithdrawnDetails(), "Withdrawn details should be null on create");

        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createAndSendConsent_withNullLeadAndContactIdentifier_savesAndPublishesEvent() {
        // Arrange
        CreateAndSendConsent command = CreateAndSendConsent.builder()
                .personId(TEST_PERSON_ID)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(null)
                .contactIdentifier(null)
                .recipientPhone(TEST_PHONE)
                .build();

        Consent savedConsent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.saveWithException(any(Consent.class))).thenReturn(savedConsent);

        // Act
        Consent result = consentWriteService.createAndSendConsent(command);

        // Assert
        assertNotNull(result, "Saved consent should not be null even with null lead/contact identifiers");
        verify(consentRepositoryWrapper).saveWithException(any(Consent.class));
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    // ==================== createConsentReceived() Tests ====================

    @Test
    void createConsentReceived_withRequestMetadata_savesConsentWithAuditId() {
        // Arrange
        String auditId = "audit-123";
        RequestContext.setRequestMetadata(RequestMetadata.builder().auditId(auditId).build());

        ConsentReceivedRequest request = ConsentReceivedRequest.builder()
                .personId(TEST_PERSON_ID)
                .type("CB")
                .build();

        Consent savedConsent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.RECEIVED);
        when(consentRepositoryWrapper.saveWithException(any(Consent.class))).thenReturn(savedConsent);

        // Act
        Consent result = consentWriteService.createConsentReceived(request);

        // Assert
        assertNotNull(result, "Saved consent should not be null");
        assertEquals(ConsentStatus.RECEIVED, result.getStatus(), "Consent should be in RECEIVED status");

        ArgumentCaptor<Consent> captor = ArgumentCaptor.forClass(Consent.class);
        verify(consentRepositoryWrapper).saveWithException(captor.capture());
        Consent captured = captor.getValue();
        assertEquals(ConsentStatus.RECEIVED, captured.getStatus(), "Consent passed to save should have RECEIVED status");
        assertNotNull(captured.getConsentReceivedDetails(), "Received details should be set");
        assertEquals(auditId, captured.getConsentReceivedDetails().getAuditId(), "Audit ID should be set from RequestContext");
        assertNull(captured.getConsentSentDetails(), "Sent details should be null for direct received consent");
        assertNull(captured.getConsentWithdrawnDetails(), "Withdrawn details should be null for direct received consent");
    }

    @Test
    void createConsentReceived_whenRequestMetadataIsNull_savesConsentWithNullAuditId() {
        // Arrange
        RequestContext.clear();

        ConsentReceivedRequest request = ConsentReceivedRequest.builder()
                .personId(TEST_PERSON_ID)
                .type("CB")
                .build();

        Consent savedConsent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.RECEIVED);
        when(consentRepositoryWrapper.saveWithException(any(Consent.class))).thenReturn(savedConsent);

        // Act
        consentWriteService.createConsentReceived(request);

        // Assert
        ArgumentCaptor<Consent> captor = ArgumentCaptor.forClass(Consent.class);
        verify(consentRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getConsentReceivedDetails().getAuditId(),
                "Audit ID should be null when RequestMetadata is absent");
    }

    // ==================== acceptConsent() Tests ====================

    @Test
    void acceptConsent_whenStatusIsSent_andRecipientInfoProvided_transitionsAndPublishesEvent() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        AcceptConsentRequest command = AcceptConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .personId(TEST_PERSON_ID)
                .recipientPhone(TEST_PHONE)
                .build();

        // Act
        consentWriteService.acceptConsent(command);

        // Assert
        assertEquals(ConsentStatus.RECEIVED, consent.getStatus(), "Status should transition to RECEIVED");
        assertNotNull(consent.getConsentReceivedDetails(), "Received details should be set after accept");
        verify(consentRepositoryWrapper).saveWithException(consent);
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void acceptConsent_whenStatusIsSent_andRecipientPhoneIsNull_transitionsWithoutPublishingEvent() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        AcceptConsentRequest command = AcceptConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .personId(TEST_PERSON_ID)
                .recipientPhone(null)
                .build();

        // Act
        consentWriteService.acceptConsent(command);

        // Assert
        assertEquals(ConsentStatus.RECEIVED, consent.getStatus(), "Status should transition to RECEIVED");
        verify(consentRepositoryWrapper).saveWithException(consent);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void acceptConsent_whenStatusIsSent_andPersonIdIsNull_transitionsWithoutPublishingEvent() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        AcceptConsentRequest command = AcceptConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .personId(null)
                .recipientPhone(TEST_PHONE)
                .build();

        // Act
        consentWriteService.acceptConsent(command);

        // Assert
        assertEquals(ConsentStatus.RECEIVED, consent.getStatus(), "Status should transition to RECEIVED even without personId");
        verify(consentRepositoryWrapper).saveWithException(consent);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void acceptConsent_whenStatusIsNotSent_throwsConsentOperationException() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.RECEIVED);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Invalid status");

        AcceptConsentRequest command = AcceptConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .build();

        // Act & Assert
        assertThrows(ConsentOperationException.class,
                () -> consentWriteService.acceptConsent(command),
                "Should throw exception when consent is not in SENT status");
        verify(consentRepositoryWrapper, never()).saveWithException(any());
        verifyNoInteractions(eventPublisher);
    }

    // ==================== resendConsent() Tests ====================

    @Test
    void resendConsent_whenStatusIsSent_updatesAndPublishesEvent() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        ResendConsentRequest request = ResendConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .personId(TEST_PERSON_ID)
                .recipientPhone(TEST_PHONE)
                .build();

        // Act
        consentWriteService.resendConsent(request);

        // Assert
        assertNotNull(consent.getConsentSentDetails(), "Sent details should be updated on resend");
        verify(consentRepositoryWrapper).saveWithException(consent);
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void resendConsent_whenStatusIsNotSent_throwsConsentOperationException() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.RECEIVED);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Invalid status");

        ResendConsentRequest request = ResendConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .build();

        // Act & Assert
        assertThrows(ConsentOperationException.class,
                () -> consentWriteService.resendConsent(request),
                "Should throw exception when consent is not in SENT status");
        verify(consentRepositoryWrapper, never()).saveWithException(any());
        verifyNoInteractions(eventPublisher);
    }

    // ==================== withdrawConsent() Tests ====================

    @Test
    void withdrawConsent_whenStatusIsReceived_transitionsToRequestForWithdrawal() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.RECEIVED);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        WithdrawConsentRequest command = WithdrawConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .build();

        // Act
        consentWriteService.withdrawConsent(command);

        // Assert
        assertEquals(ConsentStatus.REQUEST_FOR_WITHDRAWAL, consent.getStatus(),
                "Status should transition to REQUEST_FOR_WITHDRAWAL");
        assertNotNull(consent.getConsentWithdrawnDetails(), "Withdrawn details should be set");
        verify(consentRepositoryWrapper).saveWithException(consent);
    }

    @Test
    void withdrawConsent_whenStatusIsReceived_andRequestMetadataPresent_setsAuditId() {
        // Arrange
        String auditId = "audit-456";
        RequestContext.setRequestMetadata(RequestMetadata.builder().auditId(auditId).build());

        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.RECEIVED);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        WithdrawConsentRequest command = WithdrawConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .build();

        // Act
        consentWriteService.withdrawConsent(command);

        // Assert
        assertNotNull(consent.getConsentWithdrawnDetails(), "Withdrawn details should be set");
        assertEquals(auditId, consent.getConsentWithdrawnDetails().getAuditId(),
                "Audit ID should be captured from RequestContext");
        verify(consentRepositoryWrapper).saveWithException(consent);
    }

    @Test
    void withdrawConsent_whenStatusIsNotReceived_throwsConsentOperationException() {
        // Arrange
        Consent consent = buildConsent(TEST_CONSENT_ID, consentIdentifier, ConsentStatus.SENT);
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Invalid status");

        WithdrawConsentRequest command = WithdrawConsentRequest.builder()
                .consentIdentifier(consentIdentifier)
                .enquiryIdentifier(enquiryIdentifier)
                .build();

        // Act & Assert
        assertThrows(ConsentOperationException.class,
                () -> consentWriteService.withdrawConsent(command),
                "Should throw exception when consent is not in RECEIVED status");
        verify(consentRepositoryWrapper, never()).saveWithException(any());
    }

    // ==================== Helper Methods ====================

    private Consent buildConsent(Long id, UUID identifier, ConsentStatus status) {
        Consent consent = new Consent();
        consent.setId(id);
        consent.setIdentifier(identifier);
        consent.setStatus(status);
        return consent;
    }
}
