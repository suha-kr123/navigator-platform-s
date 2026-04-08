package com.nivasafinance.features.lead.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.bre.service.BREExecutionService;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadbre.entity.LeadBREResult;
import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import com.nivasafinance.features.leadbre.repository.LeadBREResultRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadBREResultWriteServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private LeadBREResultRepositoryWrapper leadBREResultRepositoryWrapper;

    @Mock
    private BREExecutionService breExecutionService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LeadBREResultWriteServiceImpl leadBREResultWriteService;

    private UUID leadIdentifier;
    private Long leadId;
    private Lead lead;
    private UUID resultIdentifier;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        leadId = 1L;
        resultIdentifier = UUID.randomUUID();

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
    }

    // ==================== executeBre() Tests ====================

    @Test
    void executeBre_success_returnsResponseWithIdentifier() {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class))).thenReturn(savedEntity);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When
        LeadBREResultExecuteResponse result = leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        assertNotNull(result, "Response should not be null");
        assertEquals(resultIdentifier, result.getIdentifier(), "Response should contain the identifier of the saved BRE result");
    }

    @Test
    void executeBre_success_savesInitialPendingRecord() {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        ArgumentCaptor<LeadBREResult> captor = ArgumentCaptor.forClass(LeadBREResult.class);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(captor.capture())).thenReturn(savedEntity);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        LeadBREResult captured = captor.getValue();
        assertEquals(leadId, captured.getLeadId(), "Pending record should use the lead's DB id");
        assertEquals(config, captured.getConfigName(), "Pending record should use the requested config name");
        assertEquals(LeadBREResultStatus.IN_PROGRESS, captured.getStatus(), "Initial record should have IN_PROGRESS status");
    }

    @Test
    void executeBre_success_invokesBREExecutionService() {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class))).thenReturn(savedEntity);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        verify(breExecutionService).execute(eq(config), any(BREExecutionRequest.class));
    }

    @Test
    void executeBre_leadNotFound_throwsException() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadBREResultWriteService.executeBre(leadIdentifier, "eligibility"),
                "Should propagate exception when lead is not found");
        verifyNoInteractions(leadBREResultRepositoryWrapper);
        verifyNoInteractions(breExecutionService);
    }

    @Test
    void executeBre_saveFails_throwsException() {
        // Given
        String config = "eligibility";
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenThrow(new RuntimeException("Save failed"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadBREResultWriteService.executeBre(leadIdentifier, config),
                "Should propagate exception when initial save fails");
        verifyNoInteractions(breExecutionService);
    }

    // ==================== whenComplete callback Tests ====================

    @Test
    void executeBre_breSuccessResponse_updatesStatusToSuccess() throws JsonProcessingException {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        LeadBREResult toUpdate = createPendingEntity(resultIdentifier);

        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .request(Map.of("key", "val"))
                .response(Map.of("result", "ok"))
                .error(null)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenReturn(savedEntity)
                .thenReturn(toUpdate);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(toUpdate);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("{\"serialized\":true}");

        Lead leadForUpdate = new Lead();
        leadForUpdate.setId(leadId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(leadForUpdate);

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        assertEquals(LeadBREResultStatus.SUCCESS, toUpdate.getStatus(), "Status should be updated to SUCCESS on successful BRE response");
        verify(leadBREResultRepositoryWrapper).findByLeadIdAndIdentifierWithException(leadId, resultIdentifier);
    }

    @Test
    void executeBre_breErrorResponse_updatesStatusToFailed() throws JsonProcessingException {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        LeadBREResult toUpdate = createPendingEntity(resultIdentifier);

        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .request(Map.of("key", "val"))
                .error("BRE execution error")
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenReturn(savedEntity)
                .thenReturn(toUpdate);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(toUpdate);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("{\"error\":\"BRE execution error\"}");

        Lead leadForUpdate = new Lead();
        leadForUpdate.setId(leadId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(leadForUpdate);

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        assertEquals(LeadBREResultStatus.FAILED, toUpdate.getStatus(), "Status should be FAILED when BRE response contains an error");
    }

    @Test
    void executeBre_breThrowsException_updatesStatusToFailed() throws JsonProcessingException {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        LeadBREResult toUpdate = createPendingEntity(resultIdentifier);

        CompletableFuture<BREExecutionResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("BRE service unavailable"));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenReturn(savedEntity)
                .thenReturn(toUpdate);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(failedFuture);
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(toUpdate);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("{\"error\":\"BRE service unavailable\"}");

        Lead leadForUpdate = new Lead();
        leadForUpdate.setId(leadId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(leadForUpdate);

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        assertEquals(LeadBREResultStatus.FAILED, toUpdate.getStatus(), "Status should be FAILED when BRE execution throws an exception");
    }

    @Test
    void executeBre_breNullResponse_updatesStatusToFailed() {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        LeadBREResult toUpdate = createPendingEntity(resultIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenReturn(savedEntity)
                .thenReturn(toUpdate);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(toUpdate);

        Lead leadForUpdate = new Lead();
        leadForUpdate.setId(leadId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(leadForUpdate);

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        assertEquals(LeadBREResultStatus.FAILED, toUpdate.getStatus(), "Status should be FAILED when BRE response is null");
    }

    @Test
    void executeBre_eligibilityConfig_updatesLeadBREExecutions() throws JsonProcessingException {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        LeadBREResult toUpdate = createPendingEntity(resultIdentifier);

        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .response(Map.of("result", "ok"))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenReturn(savedEntity)
                .thenReturn(toUpdate);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(toUpdate);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("{}");

        Lead leadForUpdate = new Lead();
        leadForUpdate.setId(leadId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(leadForUpdate);

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        verify(leadRepositoryWrapper).findByIdWithException(leadId);
        verify(leadRepositoryWrapper).saveWithException(leadForUpdate);
        assertNotNull(leadForUpdate.getBreExecutions(), "BRE executions should be set on lead");
        assertNotNull(leadForUpdate.getBreExecutions().getEligibility(), "Eligibility summary should be set");
        assertEquals(resultIdentifier, leadForUpdate.getBreExecutions().getEligibility().getResultIdentifier(),
                "Eligibility summary should reference the BRE result identifier");
    }

    @Test
    void executeBre_existingBreExecutions_preservesExistingAndUpdatesEligibility() throws JsonProcessingException {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);
        LeadBREResult toUpdate = createPendingEntity(resultIdentifier);

        BREExecutionResponse breResponse = BREExecutionResponse.builder()
                .response(Map.of("result", "ok"))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class)))
                .thenReturn(savedEntity)
                .thenReturn(toUpdate);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(breResponse));
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(toUpdate);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("{}");

        Lead leadForUpdate = new Lead();
        leadForUpdate.setId(leadId);
        Lead.BREExecutions existingExecutions = new Lead.BREExecutions();
        leadForUpdate.setBreExecutions(existingExecutions);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(leadForUpdate);

        // When
        leadBREResultWriteService.executeBre(leadIdentifier, config);

        // Then
        assertSame(existingExecutions, leadForUpdate.getBreExecutions(),
                "Should reuse existing BREExecutions object rather than creating a new one");
        assertNotNull(leadForUpdate.getBreExecutions().getEligibility(),
                "Eligibility summary should be set on existing executions");
    }

    @Test
    void executeBre_callbackPersistFails_doesNotPropagateException() {
        // Given
        String config = "eligibility";
        LeadBREResult savedEntity = createPendingEntity(resultIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.saveWithException(any(LeadBREResult.class))).thenReturn(savedEntity);
        when(breExecutionService.execute(eq(config), any(BREExecutionRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenThrow(new RuntimeException("DB error in callback"));

        // When & Then — no exception should propagate from the callback
        assertDoesNotThrow(
                () -> leadBREResultWriteService.executeBre(leadIdentifier, config),
                "Callback persistence failure should be caught internally and not propagate");
    }

    // ==================== Helper Methods ====================

    private LeadBREResult createPendingEntity(UUID identifier) {
        return LeadBREResult.builder()
                .leadId(leadId)
                .identifier(identifier)
                .configName("eligibility")
                .status(LeadBREResultStatus.IN_PROGRESS)
                .build();
    }
}
