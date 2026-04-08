package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.LeadBREResultResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadbre.entity.LeadBREResult;
import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import com.nivasafinance.features.leadbre.exception.LeadBREResultNotFoundException;
import com.nivasafinance.features.leadbre.repository.LeadBREResultRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadBREResultReadServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private LeadBREResultRepositoryWrapper leadBREResultRepositoryWrapper;

    @InjectMocks
    private LeadBREResultReadServiceImpl leadBREResultReadService;

    private UUID leadIdentifier;
    private Long leadId;
    private Lead lead;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        leadId = 1L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
    }

    // ==================== getResults(UUID) Tests ====================

    @Test
    void getResults_byLeadId_success_returnsAllResults() {
        // Given
        LeadBREResult entity1 = createEntity(UUID.randomUUID(), LeadBREResultStatus.SUCCESS, "{\"input\":1}", "{\"output\":1}");
        LeadBREResult entity2 = createEntity(UUID.randomUUID(), LeadBREResultStatus.FAILED, "{\"input\":2}", "{\"error\":\"msg\"}");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdWithException(leadId)).thenReturn(List.of(entity1, entity2));

        // When
        List<LeadBREResultResponse> result = leadBREResultReadService.getResults(leadIdentifier);

        // Then
        assertEquals(2, result.size(), "Should return all BRE results for the lead");
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadBREResultRepositoryWrapper).findByLeadIdWithException(leadId);
    }

    @Test
    void getResults_byLeadId_success_mapsFieldsCorrectly() {
        // Given
        UUID resultIdentifier = UUID.randomUUID();
        LeadBREResult entity = createEntity(resultIdentifier, LeadBREResultStatus.SUCCESS, "{\"key\":\"val\"}", "{\"out\":\"ok\"}");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdWithException(leadId)).thenReturn(List.of(entity));

        // When
        List<LeadBREResultResponse> result = leadBREResultReadService.getResults(leadIdentifier);

        // Then
        assertEquals(1, result.size(), "Should return exactly one result");
        LeadBREResultResponse response = result.get(0);
        assertEquals(resultIdentifier, response.getIdentifier(), "Response identifier should match entity identifier");
        assertEquals(LeadBREResultStatus.SUCCESS, response.getStatus(), "Response status should match entity status");
        assertEquals("{\"key\":\"val\"}", response.getInput(), "Response input should match entity input");
        assertEquals("{\"out\":\"ok\"}", response.getOutput(), "Response output should match entity output");
    }

    @Test
    void getResults_byLeadId_emptyList_returnsEmptyList() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdWithException(leadId)).thenReturn(Collections.emptyList());

        // When
        List<LeadBREResultResponse> result = leadBREResultReadService.getResults(leadIdentifier);

        // Then
        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no BRE results exist");
    }

    @Test
    void getResults_byLeadId_leadNotFound_throwsException() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadBREResultReadService.getResults(leadIdentifier),
                "Should propagate exception when lead is not found");
        verifyNoInteractions(leadBREResultRepositoryWrapper);
    }

    // ==================== getResults(UUID, String) Tests ====================

    @Test
    void getResults_byLeadIdAndConfig_success_returnsFilteredResults() {
        // Given
        String config = "eligibility";
        LeadBREResult entity = createEntity(UUID.randomUUID(), LeadBREResultStatus.SUCCESS, "{}", "{}");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdAndConfigNameWithException(leadId, config))
                .thenReturn(List.of(entity));

        // When
        List<LeadBREResultResponse> result = leadBREResultReadService.getResults(leadIdentifier, config);

        // Then
        assertEquals(1, result.size(), "Should return results filtered by config name");
        verify(leadBREResultRepositoryWrapper).findByLeadIdAndConfigNameWithException(leadId, config);
    }

    @Test
    void getResults_byLeadIdAndConfig_emptyList_returnsEmptyList() {
        // Given
        String config = "eligibility";
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdAndConfigNameWithException(leadId, config))
                .thenReturn(Collections.emptyList());

        // When
        List<LeadBREResultResponse> result = leadBREResultReadService.getResults(leadIdentifier, config);

        // Then
        assertNotNull(result, "Result list should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when no results match the config");
    }

    @Test
    void getResults_byLeadIdAndConfig_leadNotFound_throwsException() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadBREResultReadService.getResults(leadIdentifier, "eligibility"),
                "Should propagate exception when lead is not found");
        verifyNoInteractions(leadBREResultRepositoryWrapper);
    }

    // ==================== getResultByIdentifier(UUID, UUID) Tests ====================

    @Test
    void getResultByIdentifier_success_returnsResponse() {
        // Given
        UUID resultIdentifier = UUID.randomUUID();
        LeadBREResult entity = createEntity(resultIdentifier, LeadBREResultStatus.SUCCESS, "{\"in\":1}", "{\"out\":1}");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(entity);

        // When
        LeadBREResultResponse result = leadBREResultReadService.getResultByIdentifier(leadIdentifier, resultIdentifier);

        // Then
        assertNotNull(result, "Response should not be null for existing result");
        assertEquals(resultIdentifier, result.getIdentifier(), "Response identifier should match requested identifier");
        assertEquals(LeadBREResultStatus.SUCCESS, result.getStatus(), "Response status should match entity status");
    }

    @Test
    void getResultByIdentifier_resultNotFound_throwsException() {
        // Given
        UUID resultIdentifier = UUID.randomUUID();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenThrow(new LeadBREResultNotFoundException("Not found"));

        // When & Then
        assertThrows(LeadBREResultNotFoundException.class,
                () -> leadBREResultReadService.getResultByIdentifier(leadIdentifier, resultIdentifier),
                "Should throw LeadBREResultNotFoundException when result does not exist");
    }

    @Test
    void getResultByIdentifier_leadNotFound_throwsException() {
        // Given
        UUID resultIdentifier = UUID.randomUUID();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadBREResultReadService.getResultByIdentifier(leadIdentifier, resultIdentifier),
                "Should propagate exception when lead is not found");
        verifyNoInteractions(leadBREResultRepositoryWrapper);
    }

    @Test
    void getResultByIdentifier_success_mapsNullInputAndOutput() {
        // Given
        UUID resultIdentifier = UUID.randomUUID();
        LeadBREResult entity = createEntity(resultIdentifier, LeadBREResultStatus.IN_PROGRESS, null, null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(leadId, resultIdentifier))
                .thenReturn(entity);

        // When
        LeadBREResultResponse result = leadBREResultReadService.getResultByIdentifier(leadIdentifier, resultIdentifier);

        // Then
        assertNotNull(result, "Response should not be null");
        assertNull(result.getInput(), "Input should be null when entity input is null");
        assertNull(result.getOutput(), "Output should be null when entity output is null");
        assertEquals(LeadBREResultStatus.IN_PROGRESS, result.getStatus(), "Status should be IN_PROGRESS");
    }

    // ==================== Helper Methods ====================

    private LeadBREResult createEntity(UUID identifier, LeadBREResultStatus status, String input, String output) {
        return LeadBREResult.builder()
                .identifier(identifier)
                .leadId(leadId)
                .status(status)
                .input(input)
                .output(output)
                .build();
    }
}
