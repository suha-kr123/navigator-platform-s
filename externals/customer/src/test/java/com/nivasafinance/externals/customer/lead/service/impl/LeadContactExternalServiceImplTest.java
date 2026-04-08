package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadContactExternalServiceImplTest {

    private static final UUID LEAD_ID = UUID.randomUUID();
    private static final UUID CONTACT_IDENTIFIER = UUID.randomUUID();
    private static final String PAN_VALUE = "ABCDE1234F";

    @Mock
    private LeadContactReadService leadContactReadService;
    @Mock
    private LeadContactWriteService leadContactWriteService;

    @InjectMocks
    private LeadContactExternalServiceImpl leadContactExternalService;

    // --- addOrReplacePan tests ---

    @Test
    void addOrReplacePan_whenNoPanExists_addsNewIdentifier() {
        // Arrange
        IdentifierData nonPanIdentifier = IdentifierData.builder()
                .id(UUID.randomUUID())
                .type(IdentifierType.AADHAAR)
                .identifier("123456789012")
                .build();
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of(nonPanIdentifier));

        IdentifierData addedPan = IdentifierData.builder()
                .id(UUID.randomUUID())
                .type(IdentifierType.PAN)
                .identifier(PAN_VALUE)
                .build();
        when(leadContactWriteService.addIdentifier(eq(LEAD_ID), eq(CONTACT_IDENTIFIER), any(IdentifierRequest.class)))
                .thenReturn(addedPan);

        // Act
        IdentifierData result = leadContactExternalService.addOrReplacePan(LEAD_ID, CONTACT_IDENTIFIER, PAN_VALUE);

        // Assert
        assertNotNull(result, "Should return the newly added identifier");
        assertEquals(IdentifierType.PAN, result.getType(), "Type should be PAN");
        assertEquals(PAN_VALUE, result.getIdentifier(), "Identifier value should match");
        verify(leadContactWriteService).addIdentifier(eq(LEAD_ID), eq(CONTACT_IDENTIFIER), any(IdentifierRequest.class));
        verify(leadContactWriteService, never()).updateIdentifier(any(), any(), any(), any());
    }

    @Test
    void addOrReplacePan_whenPanExists_updatesExistingIdentifier() {
        // Arrange
        UUID existingPanId = UUID.randomUUID();
        IdentifierData existingPan = IdentifierData.builder()
                .id(existingPanId)
                .type(IdentifierType.PAN)
                .identifier("OLDPN1234X")
                .build();
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of(existingPan));

        IdentifierData updatedPan = IdentifierData.builder()
                .id(existingPanId)
                .type(IdentifierType.PAN)
                .identifier(PAN_VALUE)
                .build();
        when(leadContactReadService.getIdentifier(LEAD_ID, CONTACT_IDENTIFIER, existingPanId))
                .thenReturn(updatedPan);

        // Act
        IdentifierData result = leadContactExternalService.addOrReplacePan(LEAD_ID, CONTACT_IDENTIFIER, PAN_VALUE);

        // Assert
        assertNotNull(result, "Should return the updated identifier");
        assertEquals(PAN_VALUE, result.getIdentifier(), "Identifier value should be the new PAN");
        verify(leadContactWriteService).updateIdentifier(eq(LEAD_ID), eq(CONTACT_IDENTIFIER), eq(existingPanId), any(IdentifierRequest.class));
        verify(leadContactReadService).getIdentifier(LEAD_ID, CONTACT_IDENTIFIER, existingPanId);
        verify(leadContactWriteService, never()).addIdentifier(any(), any(), any());
    }

    @Test
    void addOrReplacePan_whenEmptyIdentifiersList_addsNewIdentifier() {
        // Arrange
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of());

        IdentifierData addedPan = IdentifierData.builder()
                .id(UUID.randomUUID())
                .type(IdentifierType.PAN)
                .identifier(PAN_VALUE)
                .build();
        when(leadContactWriteService.addIdentifier(eq(LEAD_ID), eq(CONTACT_IDENTIFIER), any(IdentifierRequest.class)))
                .thenReturn(addedPan);

        // Act
        IdentifierData result = leadContactExternalService.addOrReplacePan(LEAD_ID, CONTACT_IDENTIFIER, PAN_VALUE);

        // Assert
        assertNotNull(result, "Should return the newly added identifier");
        verify(leadContactWriteService).addIdentifier(eq(LEAD_ID), eq(CONTACT_IDENTIFIER), any(IdentifierRequest.class));
    }

    @Test
    void addOrReplacePan_whenMultipleIdentifiersWithPan_updatesFirstPan() {
        // Arrange
        UUID existingPanId = UUID.randomUUID();
        IdentifierData aadhar = IdentifierData.builder()
                .id(UUID.randomUUID())
                .type(IdentifierType.AADHAAR)
                .identifier("123456789012")
                .build();
        IdentifierData existingPan = IdentifierData.builder()
                .id(existingPanId)
                .type(IdentifierType.PAN)
                .identifier("OLDPN1234X")
                .build();
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of(aadhar, existingPan));

        IdentifierData updatedPan = IdentifierData.builder()
                .id(existingPanId)
                .type(IdentifierType.PAN)
                .identifier(PAN_VALUE)
                .build();
        when(leadContactReadService.getIdentifier(LEAD_ID, CONTACT_IDENTIFIER, existingPanId))
                .thenReturn(updatedPan);

        // Act
        IdentifierData result = leadContactExternalService.addOrReplacePan(LEAD_ID, CONTACT_IDENTIFIER, PAN_VALUE);

        // Assert
        assertEquals(existingPanId, result.getId(), "Should update the existing PAN identifier");
        verify(leadContactWriteService).updateIdentifier(eq(LEAD_ID), eq(CONTACT_IDENTIFIER), eq(existingPanId), any(IdentifierRequest.class));
    }

    // --- getPan tests ---

    @Test
    void getPan_whenPanExists_returnsOptionalWithPan() {
        // Arrange
        IdentifierData panIdentifier = IdentifierData.builder()
                .id(UUID.randomUUID())
                .type(IdentifierType.PAN)
                .identifier(PAN_VALUE)
                .build();
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of(panIdentifier));

        // Act
        Optional<IdentifierData> result = leadContactExternalService.getPan(LEAD_ID, CONTACT_IDENTIFIER);

        // Assert
        assertTrue(result.isPresent(), "Should return a non-empty Optional");
        assertEquals(IdentifierType.PAN, result.get().getType(), "Type should be PAN");
        assertEquals(PAN_VALUE, result.get().getIdentifier(), "Identifier value should match");
    }

    @Test
    void getPan_whenNoPanExists_returnsEmptyOptional() {
        // Arrange
        IdentifierData aadhar = IdentifierData.builder()
                .id(UUID.randomUUID())
                .type(IdentifierType.AADHAAR)
                .identifier("123456789012")
                .build();
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of(aadhar));

        // Act
        Optional<IdentifierData> result = leadContactExternalService.getPan(LEAD_ID, CONTACT_IDENTIFIER);

        // Assert
        assertTrue(result.isEmpty(), "Should return an empty Optional when no PAN exists");
    }

    @Test
    void getPan_whenNoIdentifiers_returnsEmptyOptional() {
        // Arrange
        when(leadContactReadService.getIdentifiers(LEAD_ID, CONTACT_IDENTIFIER))
                .thenReturn(List.of());

        // Act
        Optional<IdentifierData> result = leadContactExternalService.getPan(LEAD_ID, CONTACT_IDENTIFIER);

        // Assert
        assertTrue(result.isEmpty(), "Should return an empty Optional when no identifiers exist");
    }
}
