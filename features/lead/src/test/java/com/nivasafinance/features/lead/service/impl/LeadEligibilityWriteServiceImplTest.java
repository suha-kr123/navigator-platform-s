package com.nivasafinance.features.lead.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadEligibilityWriteServiceImplTest {

    @Mock
    private LeadBREResultWriteService leadBREResultWriteService;

    @Mock
    private DataProviderExecutor dataProviderExecutor;

    @Mock
    private LeadWriteService leadWriteService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

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
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        LeadBREResultExecuteResponse result = leadEligibilityWriteService.executeEligibility(leadId);

        assertNotNull(result);
        assertNotNull(result.getIdentifier());
        verify(leadBREResultWriteService).executeBre(eq(leadId), eq("eligibility"), any(UUID.class));
        verifyNoMoreInteractions(leadBREResultWriteService);
    }

    @Test
    void executeEligibility_bureauRejectProfile_rejectsLead() {
        Map<String, Object> responseMap = Map.of("profile_match", Map.of("profile_name", "bureau_reject"));
        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .response(responseMap)
                .build();
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        leadEligibilityWriteService.executeEligibility(leadId);

        verify(leadWriteService).rejectLead(eq(leadId), any(RejectLeadRequest.class));
    }

    @Test
    void executeEligibility_differentProfile_doesNotRejectLead() {
        Map<String, Object> responseMap = Map.of("profile_match", Map.of("profile_name", "approved"));
        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .response(responseMap)
                .build();
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        leadEligibilityWriteService.executeEligibility(leadId);

        verifyNoInteractions(leadWriteService);
    }

    @Test
    void executeEligibility_missingProfileName_doesNotRejectLead() {
        Map<String, Object> responseMap = Map.of("profile_match", Map.of());
        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .response(responseMap)
                .build();
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        leadEligibilityWriteService.executeEligibility(leadId);

        verifyNoInteractions(leadWriteService);
    }

    @Test
    void executeEligibility_breResponseWithError_doesNotRejectLead() {
        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .error("BRE execution error")
                .build();
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));

        leadEligibilityWriteService.executeEligibility(leadId);

        verifyNoInteractions(leadWriteService);
    }

    @Test
    void executeEligibility_nullResponse_doesNotRejectLead() {
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        leadEligibilityWriteService.executeEligibility(leadId);

        verifyNoInteractions(leadWriteService);
    }

    // ==================== executeEligibilityOnStageTransition() Tests ====================

    @Test
    void executeEligibilityOnStageTransition_whenShouldExecuteTrue_executesBreEligibility() {
        when(dataProviderExecutor.executeDataProvider(
                "lead_bre_eligibility_trigger",
                Map.of("leadIdentifier", leadId)
        )).thenReturn(Map.of("should_execute", "true"));
        when(leadBREResultWriteService.executeBre(eq(leadId), eq("eligibility"), any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        leadEligibilityWriteService.executeEligibilityOnStageTransition(leadId);

        verify(leadBREResultWriteService).executeBre(eq(leadId), eq("eligibility"), any(UUID.class));
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
