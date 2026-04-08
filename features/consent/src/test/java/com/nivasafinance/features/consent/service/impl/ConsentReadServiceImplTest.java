package com.nivasafinance.features.consent.service.impl;

import com.nivasafinance.features.consent.dto.ConsentResponse;
import com.nivasafinance.features.consent.entity.Consent;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.consent.exception.ConsentNotFoundException;
import com.nivasafinance.features.consent.repository.ConsentRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentReadServiceImplTest {

    @Mock
    private ConsentRepositoryWrapper consentRepositoryWrapper;

    @InjectMocks
    private ConsentReadServiceImpl consentReadService;

    private Consent consent;
    private Long consentId;
    private UUID consentIdentifier;

    @BeforeEach
    void setUp() {
        consentId = 1L;
        consentIdentifier = UUID.randomUUID();

        consent = new Consent();
        consent.setId(consentId);
        consent.setIdentifier(consentIdentifier);
        consent.setStatus(ConsentStatus.SENT);
        consent.setConsentSentDetails(new Consent.ConsentSentDetails(LocalDateTime.now()));
    }

    // ==================== findById() Tests ====================

    @Test
    void findById_whenConsentExists_returnsOptionalWithConsentResponse() {
        // Arrange
        when(consentRepositoryWrapper.findById(consentId)).thenReturn(Optional.of(consent));

        // Act
        Optional<ConsentResponse> result = consentReadService.findById(consentId);

        // Assert
        assertTrue(result.isPresent(), "Should return a present Optional when consent exists");
        assertEquals(consentId, result.get().getId(), "Response ID should match the consent ID");
        assertEquals(consentIdentifier, result.get().getIdentifier(), "Response identifier should match the consent identifier");
        assertEquals(ConsentStatus.SENT, result.get().getStatus(), "Response status should match the consent status");
        verify(consentRepositoryWrapper).findById(consentId);
    }

    @Test
    void findById_whenConsentNotFound_returnsEmptyOptional() {
        // Arrange
        when(consentRepositoryWrapper.findById(consentId)).thenReturn(Optional.empty());

        // Act
        Optional<ConsentResponse> result = consentReadService.findById(consentId);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty Optional when consent does not exist");
        verify(consentRepositoryWrapper).findById(consentId);
    }

    // ==================== findByIdWithException() Tests ====================

    @Test
    void findByIdWithException_whenConsentExists_returnsConsentResponse() {
        // Arrange
        when(consentRepositoryWrapper.findByIdWithException(consentId)).thenReturn(consent);

        // Act
        ConsentResponse result = consentReadService.findByIdWithException(consentId);

        // Assert
        assertNotNull(result, "Should return a non-null response when consent exists");
        assertEquals(consentId, result.getId(), "Response ID should match the consent ID");
        assertEquals(consentIdentifier, result.getIdentifier(), "Response identifier should match the consent identifier");
        assertEquals(ConsentStatus.SENT, result.getStatus(), "Response status should match the consent status");
        verify(consentRepositoryWrapper).findByIdWithException(consentId);
    }

    @Test
    void findByIdWithException_whenConsentNotFound_propagatesConsentNotFoundException() {
        // Arrange
        when(consentRepositoryWrapper.findByIdWithException(consentId))
                .thenThrow(new ConsentNotFoundException("Consent not found"));

        // Act & Assert
        assertThrows(ConsentNotFoundException.class,
                () -> consentReadService.findByIdWithException(consentId),
                "Should propagate ConsentNotFoundException from repository wrapper");
        verify(consentRepositoryWrapper).findByIdWithException(consentId);
    }

    // ==================== findByIdentifierWithException() Tests ====================

    @Test
    void findByIdentifierWithException_whenConsentExists_returnsConsentResponse() {
        // Arrange
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier)).thenReturn(consent);

        // Act
        ConsentResponse result = consentReadService.findByIdentifierWithException(consentIdentifier);

        // Assert
        assertNotNull(result, "Should return a non-null response when consent exists");
        assertEquals(consentId, result.getId(), "Response ID should match the consent ID");
        assertEquals(consentIdentifier, result.getIdentifier(), "Response identifier should match");
        assertEquals(ConsentStatus.SENT, result.getStatus(), "Response status should match the consent status");
        verify(consentRepositoryWrapper).findByIdentifierWithException(consentIdentifier);
    }

    @Test
    void findByIdentifierWithException_whenConsentNotFound_propagatesConsentNotFoundException() {
        // Arrange
        when(consentRepositoryWrapper.findByIdentifierWithException(consentIdentifier))
                .thenThrow(new ConsentNotFoundException("Consent not found"));

        // Act & Assert
        assertThrows(ConsentNotFoundException.class,
                () -> consentReadService.findByIdentifierWithException(consentIdentifier),
                "Should propagate ConsentNotFoundException from repository wrapper");
        verify(consentRepositoryWrapper).findByIdentifierWithException(consentIdentifier);
    }

    // ==================== findById() – ConsentResponse mapping Tests ====================

    @Test
    void findById_whenConsentHasAllDetails_mapsAllFieldsToResponse() {
        // Arrange
        LocalDateTime sentTime = LocalDateTime.of(2025, 1, 15, 10, 30);
        LocalDateTime receivedTime = LocalDateTime.of(2025, 1, 16, 14, 0);
        LocalDateTime withdrawnTime = LocalDateTime.of(2025, 1, 17, 9, 0);

        consent.setStatus(ConsentStatus.RECEIVED);
        consent.setConsentSentDetails(new Consent.ConsentSentDetails(sentTime));
        consent.setConsentReceivedDetails(new Consent.ConsentReceivedDetails(receivedTime, "audit-1"));
        consent.setConsentWithdrawnDetails(new Consent.ConsentWithdrawnDetails(withdrawnTime, "audit-2"));

        when(consentRepositoryWrapper.findById(consentId)).thenReturn(Optional.of(consent));

        // Act
        Optional<ConsentResponse> result = consentReadService.findById(consentId);

        // Assert
        assertTrue(result.isPresent(), "Should return a present Optional");
        ConsentResponse response = result.get();
        assertEquals(sentTime, response.getConsentSentTime(), "Sent time should be mapped from entity");
        assertEquals(receivedTime, response.getConsentReceivedTime(), "Received time should be mapped from entity");
        assertEquals(withdrawnTime, response.getConsentWithdrawlRequestedTime(), "Withdrawn time should be mapped from entity");
    }

    @Test
    void findById_whenConsentHasNullDetails_mapsNullTimesToResponse() {
        // Arrange
        consent.setConsentSentDetails(null);
        consent.setConsentReceivedDetails(null);
        consent.setConsentWithdrawnDetails(null);

        when(consentRepositoryWrapper.findById(consentId)).thenReturn(Optional.of(consent));

        // Act
        Optional<ConsentResponse> result = consentReadService.findById(consentId);

        // Assert
        assertTrue(result.isPresent(), "Should return a present Optional even with null details");
        ConsentResponse response = result.get();
        assertNull(response.getConsentSentTime(), "Sent time should be null when sent details are null");
        assertNull(response.getConsentReceivedTime(), "Received time should be null when received details are null");
        assertNull(response.getConsentWithdrawlRequestedTime(), "Withdrawn time should be null when withdrawn details are null");
    }
}
