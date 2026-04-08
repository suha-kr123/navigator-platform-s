package com.nivasafinance.features.leadstages.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryDisplayResponse;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadStageHistoryReadServiceImplTest {

    @Mock
    private LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @InjectMocks
    private LeadStageHistoryReadServiceImpl leadStageHistoryReadService;

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

    // ==================== getStageHistoryByLeadId() Tests ====================

    @Test
    void getStageHistoryByLeadId_withValidRequest_returnsPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadStageHistory stageHistory = LeadStageHistory.builder()
                .leadId(leadId)
                .stageKey("STAGE_1")
                .enteredAt(LocalDateTime.now())
                .movedBy("test-user")
                .assignmentHistory(new ArrayList<>())
                .build();
        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(1L).build();
        PaginatedResponse<LeadStageHistory> paginatedResponse = new PaginatedResponse<>(
                List.of(stageHistory), paginationInfo);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findByLeadIdOrderByEnteredAtDesc(leadId, paginationRequest))
                .thenReturn(paginatedResponse);

        // Act
        PaginatedResponse<LeadStageHistoryResponse> result = leadStageHistoryReadService
                .getStageHistoryByLeadId(leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(1L, result.getPagination().getTotalElements(), "Total elements should match");
        assertEquals(1, result.getContent().size(), "Content list should contain one history entry");
        assertEquals("STAGE_1", result.getContent().get(0).getStageKey(),
                "Stage key should match the history entry");
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadStageHistoryRepositoryWrapper).findByLeadIdOrderByEnteredAtDesc(leadId, paginationRequest);
    }

    @Test
    void getStageHistoryByLeadId_withEmptyResult_returnsEmptyPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(0L).build();
        PaginatedResponse<LeadStageHistory> paginatedResponse = new PaginatedResponse<>(
                List.of(), paginationInfo);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findByLeadIdOrderByEnteredAtDesc(leadId, paginationRequest))
                .thenReturn(paginatedResponse);

        // Act
        PaginatedResponse<LeadStageHistoryResponse> result = leadStageHistoryReadService
                .getStageHistoryByLeadId(leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null even for empty results");
        assertEquals(0L, result.getPagination().getTotalElements(), "Total elements should be zero");
        assertTrue(result.getContent().isEmpty(), "Content list should be empty");
    }

    @Test
    void getStageHistoryByLeadId_withMultipleEntries_returnsMappedResponses() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadStageHistory entry1 = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .movedBy("user-a").assignmentHistory(new ArrayList<>()).build();
        LeadStageHistory entry2 = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_2").stageFrom("STAGE_1")
                .enteredAt(LocalDateTime.now()).movedBy("user-b")
                .assignmentHistory(new ArrayList<>()).build();

        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(2L).build();
        PaginatedResponse<LeadStageHistory> paginatedResponse = new PaginatedResponse<>(
                List.of(entry1, entry2), paginationInfo);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findByLeadIdOrderByEnteredAtDesc(leadId, paginationRequest))
                .thenReturn(paginatedResponse);

        // Act
        PaginatedResponse<LeadStageHistoryResponse> result = leadStageHistoryReadService
                .getStageHistoryByLeadId(leadIdentifier, paginationRequest);

        // Assert
        assertEquals(2, result.getContent().size(), "Content list should contain two history entries");
        assertEquals("STAGE_2", result.getContent().get(1).getStageKey(),
                "Second entry stage key should match");
        assertEquals("STAGE_1", result.getContent().get(1).getStageFrom(),
                "Second entry stageFrom should reference the first stage");
    }

    // ==================== getStageHistoryWithDisplayLabelsByLeadId() Tests ====================

    @Test
    void getStageHistoryWithDisplayLabelsByLeadId_withValidLeadId_returnsDisplayResponses() {
        // Arrange
        LeadStageHistoryDisplayResponse displayResponse = LeadStageHistoryDisplayResponse.builder()
                .displayLabel("Stage 1")
                .enteredAt(LocalDateTime.now())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findStageHistoryWithDisplayLabelsByLeadId(leadId))
                .thenReturn(List.of(displayResponse));

        // Act
        List<LeadStageHistoryDisplayResponse> result = leadStageHistoryReadService
                .getStageHistoryWithDisplayLabelsByLeadId(leadIdentifier);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(1, result.size(), "Result should contain one display response");
        assertEquals("Stage 1", result.get(0).getDisplayLabel(), "Display label should match");
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadStageHistoryRepositoryWrapper).findStageHistoryWithDisplayLabelsByLeadId(leadId);
    }

    @Test
    void getStageHistoryWithDisplayLabelsByLeadId_withEmptyResult_returnsEmptyList() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findStageHistoryWithDisplayLabelsByLeadId(leadId))
                .thenReturn(List.of());

        // Act
        List<LeadStageHistoryDisplayResponse> result = leadStageHistoryReadService
                .getStageHistoryWithDisplayLabelsByLeadId(leadIdentifier);

        // Assert
        assertNotNull(result, "Response should not be null even for empty results");
        assertTrue(result.isEmpty(), "Result list should be empty");
    }
}
