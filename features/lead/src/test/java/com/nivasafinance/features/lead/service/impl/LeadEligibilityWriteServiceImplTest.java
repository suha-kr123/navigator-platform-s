package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadEligibilityWriteServiceImplTest {

    @Mock
    private LeadBREResultWriteService leadBREResultWriteService;

    @Mock
    private DataProviderExecutor dataProviderExecutor;

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
        UUID resultIdentifier = UUID.randomUUID();
        LeadBREResultExecuteResponse expected = LeadBREResultExecuteResponse.builder()
                .identifier(resultIdentifier).build();

        when(leadBREResultWriteService.executeBre(leadId, "eligibility")).thenReturn(expected);

        LeadBREResultExecuteResponse result = leadEligibilityWriteService.executeEligibility(leadId);

        assertNotNull(result);
        assertEquals(resultIdentifier, result.getIdentifier());
        verify(leadBREResultWriteService).executeBre(leadId, "eligibility");
        verifyNoMoreInteractions(leadBREResultWriteService);
    }

    // ==================== executeEligibilityOnStageTransition() Tests ====================

    @Test
    void executeEligibilityOnStageTransition_whenShouldExecuteTrue_executesBreEligibility() {
        when(dataProviderExecutor.executeDataProvider(
                "lead_bre_eligibility_trigger",
                Map.of("leadIdentifier", leadId)
        )).thenReturn(Map.of("should_execute", "true"));

        leadEligibilityWriteService.executeEligibilityOnStageTransition(leadId);

        verify(leadBREResultWriteService).executeBre(leadId, "eligibility");
    }

    @Test
    void executeEligibilityOnStageTransition_whenShouldExecuteFalse_skipsBreExecution() {
        when(dataProviderExecutor.executeDataProvider(
                "lead_bre_eligibility_trigger",
                Map.of("leadIdentifier", leadId)
        )).thenReturn(Map.of("should_execute", "false"));

        leadEligibilityWriteService.executeEligibilityOnStageTransition(leadId);

        verifyNoInteractions(leadBREResultWriteService);
    }

    @Test
    void executeEligibilityOnStageTransition_whenResultMapEmpty_skipsBreExecution() {
        when(dataProviderExecutor.executeDataProvider(
                "lead_bre_eligibility_trigger",
                Map.of("leadIdentifier", leadId)
        )).thenReturn(Map.of());

        leadEligibilityWriteService.executeEligibilityOnStageTransition(leadId);

        verifyNoInteractions(leadBREResultWriteService);
    }
}
