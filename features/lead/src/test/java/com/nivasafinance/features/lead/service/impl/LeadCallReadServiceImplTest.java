package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.lead.dto.LeadCallLogResponse;
import com.nivasafinance.features.lead.dto.LeadCallSummaryResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadCallReadServiceImplTest {

    @Mock
    private CallReadService callReadService;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;

    @InjectMocks
    private LeadCallReadServiceImpl leadCallReadService;

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

    // ==================== getCallSummary() Tests ====================

    @Test
    void getCallSummary_withDetails_returnsResponse() {
        // Given
        Lead.CallSummaryDetails details = Lead.CallSummaryDetails.builder()
                .totalOutboundCalls(10)
                .outboundConnectedCalls(5)
                .consecutiveNoAnswers(2)
                .build();
        lead.setCallSummaryDetails(details);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // When
        LeadCallSummaryResponse result = leadCallReadService.getCallSummary(leadIdentifier);

        // Then
        assertNotNull(result, "Summary response should not be null");
        assertEquals(10, result.getOutboundConnectRate().getTotal(), "Total outbound calls should match");
        assertEquals(5, result.getOutboundConnectRate().getConnected(), "Connected outbound calls should match");
        assertEquals(2, result.getConsecutiveNoAnswers(), "Consecutive no-answers should match");
    }

    @Test
    void getCallSummary_withNullDetails_returnsEmptyResponse() {
        // Given
        lead.setCallSummaryDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // When
        LeadCallSummaryResponse result = leadCallReadService.getCallSummary(leadIdentifier);

        // Then
        assertNotNull(result, "Summary response should not be null even when details are null");
        assertEquals(0, result.getOutboundConnectRate().getTotal(), "Total should be 0 when no details exist");
        assertEquals(0, result.getOutboundConnectRate().getConnected(), "Connected should be 0 when no details exist");
    }

    @Test
    void getCallSummary_leadNotFound_throwsException() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadCallReadService.getCallSummary(leadIdentifier),
                "Should propagate exception when lead is not found");
    }

    // ==================== recalculateLeadCallSummary() Tests ====================

    @Test
    void recalculateLeadCallSummary_noLinks_setsEmptySummary() {
        // Given
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId))
                .thenReturn(Collections.emptyList());

        // When
        leadCallReadService.recalculateLeadCallSummary(leadId);

        // Then
        verify(leadRepositoryWrapper).saveWithException(lead);
        assertNotNull(lead.getCallSummaryDetails(), "Summary details should be set even when no links exist");
        assertEquals(0, lead.getCallSummaryDetails().getTotalOutboundCalls(),
                "Total outbound calls should be 0 when no call logs exist");
    }

    @Test
    void recalculateLeadCallSummary_withOutboundCalls_computesSummary() {
        // Given
        CallLogLead link1 = new CallLogLead(100L, leadId, null);
        CallLogLead link2 = new CallLogLead(101L, leadId, null);

        LocalDateTime now = LocalDateTime.now();
        CallLogResponse outboundCompleted = CallLogResponse.builder()
                .id(100L).direction(CallDirection.OUTBOUND).status(CallStatus.COMPLETED)
                .createdAt(now.minusHours(1))
                .completionDetails(CallLog.CompletionDetails.builder().duration(120L).build())
                .build();
        CallLogResponse outboundNoAnswer = CallLogResponse.builder()
                .id(101L).direction(CallDirection.OUTBOUND).status(CallStatus.NO_ANSWER)
                .createdAt(now)
                .build();

        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId))
                .thenReturn(List.of(link1, link2));
        when(callReadService.getCallLogsByIDs(List.of(100L, 101L)))
                .thenReturn(List.of(outboundCompleted, outboundNoAnswer));

        // When
        leadCallReadService.recalculateLeadCallSummary(leadId);

        // Then
        verify(leadRepositoryWrapper).saveWithException(lead);
        Lead.CallSummaryDetails details = lead.getCallSummaryDetails();
        assertNotNull(details, "Call summary details should be computed");
        assertEquals(2, details.getTotalOutboundCalls(), "Should count all outbound calls");
        assertEquals(1, details.getOutboundConnectedCalls(), "Should count only completed outbound calls as connected");
        assertEquals(1, details.getConsecutiveNoAnswers(), "Most recent outbound is NO_ANSWER so consecutive count should be 1");
    }

    @Test
    void recalculateLeadCallSummary_onlyInboundCalls_zeroOutboundMetrics() {
        // Given
        CallLogLead link = new CallLogLead(100L, leadId, null);

        CallLogResponse inbound = CallLogResponse.builder()
                .id(100L).direction(CallDirection.INBOUND).status(CallStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId))
                .thenReturn(List.of(link));
        when(callReadService.getCallLogsByIDs(List.of(100L)))
                .thenReturn(List.of(inbound));

        // When
        leadCallReadService.recalculateLeadCallSummary(leadId);

        // Then
        Lead.CallSummaryDetails details = lead.getCallSummaryDetails();
        assertEquals(0, details.getTotalOutboundCalls(), "Should be 0 when all calls are inbound");
        assertEquals(0, details.getOutboundConnectedCalls(), "Should be 0 when all calls are inbound");
        assertEquals(0, details.getConsecutiveNoAnswers(), "Should be 0 when there are no outbound calls");
    }

    @Test
    void recalculateLeadCallSummary_multipleConsecutiveNoAnswers_countsCorrectly() {
        // Given
        CallLogLead link1 = new CallLogLead(100L, leadId, null);
        CallLogLead link2 = new CallLogLead(101L, leadId, null);
        CallLogLead link3 = new CallLogLead(102L, leadId, null);

        LocalDateTime now = LocalDateTime.now();
        CallLogResponse completed = CallLogResponse.builder()
                .id(100L).direction(CallDirection.OUTBOUND).status(CallStatus.COMPLETED)
                .createdAt(now.minusHours(3)).build();
        CallLogResponse noAnswer1 = CallLogResponse.builder()
                .id(101L).direction(CallDirection.OUTBOUND).status(CallStatus.NO_ANSWER)
                .createdAt(now.minusHours(1)).build();
        CallLogResponse noAnswer2 = CallLogResponse.builder()
                .id(102L).direction(CallDirection.OUTBOUND).status(CallStatus.NO_ANSWER)
                .createdAt(now).build();

        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId))
                .thenReturn(List.of(link1, link2, link3));
        when(callReadService.getCallLogsByIDs(List.of(100L, 101L, 102L)))
                .thenReturn(List.of(completed, noAnswer1, noAnswer2));

        // When
        leadCallReadService.recalculateLeadCallSummary(leadId);

        // Then
        assertEquals(2, lead.getCallSummaryDetails().getConsecutiveNoAnswers(),
                "Should count 2 consecutive NO_ANSWER calls at the tail");
    }

    @Test
    void recalculateLeadCallSummary_withDuration_computesAverage() {
        // Given
        CallLogLead link1 = new CallLogLead(100L, leadId, null);
        CallLogLead link2 = new CallLogLead(101L, leadId, null);

        LocalDateTime now = LocalDateTime.now();
        CallLogResponse call1 = CallLogResponse.builder()
                .id(100L).direction(CallDirection.OUTBOUND).status(CallStatus.COMPLETED)
                .createdAt(now.minusHours(1))
                .completionDetails(CallLog.CompletionDetails.builder().duration(60L).build())
                .build();
        CallLogResponse call2 = CallLogResponse.builder()
                .id(101L).direction(CallDirection.OUTBOUND).status(CallStatus.COMPLETED)
                .createdAt(now)
                .completionDetails(CallLog.CompletionDetails.builder().duration(120L).build())
                .build();

        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId))
                .thenReturn(List.of(link1, link2));
        when(callReadService.getCallLogsByIDs(List.of(100L, 101L)))
                .thenReturn(List.of(call1, call2));

        // When
        leadCallReadService.recalculateLeadCallSummary(leadId);

        // Then
        assertEquals(90.0, lead.getCallSummaryDetails().getAverageOutboundTalkDurationSeconds(),
                "Average duration should be (60+120)/2 = 90 seconds");
    }

    // ==================== getCallLogs() Tests ====================

    @Test
    void getCallLogs_success_returnsPaginatedResponse() {
        // Given
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setLimit(10);
        paginationRequest.setOffset(0);

        CallLogLead link = new CallLogLead(100L, leadId, 50L);
        Page<CallLogLead> page = new PageImpl<>(List.of(link), PageRequest.of(0, 10), 1);

        CallLogResponse callLogResponse = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .direction(CallDirection.OUTBOUND).status(CallStatus.COMPLETED)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findByLeadId(eq(leadId), any(PageRequest.class))).thenReturn(page);
        when(callReadService.getCallLogsByIDs(List.of(100L))).thenReturn(List.of(callLogResponse));

        // When
        PaginatedResponse<LeadCallLogResponse> result = leadCallReadService.getCallLogs(leadIdentifier, paginationRequest, false);

        // Then
        assertNotNull(result, "Paginated response should not be null");
        assertEquals(1, result.getContent().size(), "Should return one call log entry");
        assertEquals(50L, result.getContent().get(0).getContactId(), "Contact ID should be mapped from the link");
        assertNotNull(result.getContent().get(0).getCallLogDetails(), "Call log details should be populated");
    }

    @Test
    void getCallLogs_emptyPage_returnsEmptyResponse() {
        // Given
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setLimit(10);
        paginationRequest.setOffset(0);

        Page<CallLogLead> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findByLeadId(eq(leadId), any(PageRequest.class))).thenReturn(emptyPage);

        // When
        PaginatedResponse<LeadCallLogResponse> result = leadCallReadService.getCallLogs(leadIdentifier, paginationRequest, false);

        // Then
        assertNotNull(result, "Paginated response should not be null");
        assertTrue(result.getContent().isEmpty(), "Data should be empty when no call logs exist");
        assertEquals(0, result.getPagination().getTotalElements(), "Total elements should be 0");
        assertFalse(result.getPagination().isHasNext(), "Should not have next page");
        assertFalse(result.getPagination().isHasPrevious(), "Should not have previous page");
    }

    @Test
    void getCallLogs_pagination_computesPageCorrectly() {
        // Given
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setLimit(5);
        paginationRequest.setOffset(10);

        Page<CallLogLead> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 5), 0);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findByLeadId(eq(leadId), eq(PageRequest.of(2, 5)))).thenReturn(emptyPage);

        // When
        leadCallReadService.getCallLogs(leadIdentifier, paginationRequest, false);

        // Then
        verify(callLogLeadRepositoryWrapper).findByLeadId(eq(leadId), eq(PageRequest.of(2, 5)));
    }

    @Test
    void getCallLogs_leadNotFound_throwsException() {
        // Given
        PaginationRequest paginationRequest = new PaginationRequest();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> leadCallReadService.getCallLogs(leadIdentifier, paginationRequest, false),
                "Should propagate exception when lead is not found");
        verifyNoInteractions(callLogLeadRepositoryWrapper);
    }

    // ==================== refreshCallSummary() Tests ====================

    @Test
    void refreshCallSummary_success_recalculatesAndReturns() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(callLogLeadRepositoryWrapper.findAllByLeadIdOrderByCallLogIdDesc(leadId))
                .thenReturn(Collections.emptyList());

        // When
        LeadCallSummaryResponse result = leadCallReadService.refreshCallSummary(leadIdentifier);

        // Then
        assertNotNull(result, "Refresh should return a summary response");
        verify(leadRepositoryWrapper).saveWithException(lead);
    }
}
