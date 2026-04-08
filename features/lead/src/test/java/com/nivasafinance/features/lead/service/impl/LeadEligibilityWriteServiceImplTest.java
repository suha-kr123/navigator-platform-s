package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadEligibilityWriteServiceImplTest {

    @Mock
    private LeadBREResultWriteService leadBREResultWriteService;

    @InjectMocks
    private LeadEligibilityWriteServiceImpl leadEligibilityWriteService;

    private UUID leadId;

    @BeforeEach
    void setUp() {
        leadId = UUID.randomUUID();
    }

    // ==================== executeEligibility() Tests ====================

    @Test
    void executeEligibility_withValidLeadId_delegatesToBREWriteService() {
        // Arrange
        UUID resultIdentifier = UUID.randomUUID();
        LeadBREResultExecuteResponse expected = LeadBREResultExecuteResponse.builder()
                .identifier(resultIdentifier).build();

        when(leadBREResultWriteService.executeBre(leadId, "eligibility")).thenReturn(expected);

        // Act
        LeadBREResultExecuteResponse result = leadEligibilityWriteService.executeEligibility(leadId);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(resultIdentifier, result.getIdentifier(), "Identifier should match the BRE execution result");
        verify(leadBREResultWriteService).executeBre(leadId, "eligibility");
        verifyNoMoreInteractions(leadBREResultWriteService);
    }
}
