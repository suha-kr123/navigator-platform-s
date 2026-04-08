package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorCallLogResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.service.CallReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorCallReadServiceImplTest {

    @Mock
    private CallReadService callReadService;

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @InjectMocks
    private AdvisorCallReadServiceImpl advisorCallReadService;

    private UUID advisorIdentifier;
    private Advisor advisor;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        paginationRequest = new PaginationRequest(0, 20, "createdAt", "DESC");
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
    }

    @Test
    void getCallLogs_nullCallLogDetails_returnsEmptyPaginatedResponse() {
        advisor.setCallLogDetails(null);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        PaginatedResponse<AdvisorCallLogResponse> result =
                advisorCallReadService.getCallLogs(advisorIdentifier, paginationRequest);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(callReadService, never()).getCallLogsByIDs(anyList());
    }

    @Test
    void getCallLogs_emptyCallLogDetails_returnsEmptyPaginatedResponse() {
        advisor.setCallLogDetails(List.of());
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        PaginatedResponse<AdvisorCallLogResponse> result =
                advisorCallReadService.getCallLogs(advisorIdentifier, paginationRequest);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(callReadService, never()).getCallLogsByIDs(anyList());
    }

    @Test
    void getCallLogs_withCallLogDetails_returnsMappedResponse() {
        Advisor.CallLogDetails detail1 = new Advisor.CallLogDetails();
        detail1.setCallLogId(100L);
        Advisor.CallLogDetails detail2 = new Advisor.CallLogDetails();
        detail2.setCallLogId(101L);
        advisor.setCallLogDetails(List.of(detail1, detail2));

        CallLogResponse callLogResponse = new CallLogResponse();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(callReadService.getCallLogsByIDs(List.of(101L, 100L))).thenReturn(List.of(callLogResponse, callLogResponse));

        PaginatedResponse<AdvisorCallLogResponse> result =
                advisorCallReadService.getCallLogs(advisorIdentifier, paginationRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertNotNull(result.getPagination());
        verify(callReadService).getCallLogsByIDs(List.of(101L, 100L));
    }

    @Test
    void getCallLogs_withOffset_appliesSkipAndLimitAndPaginationFlags() {
        Advisor.CallLogDetails d1 = new Advisor.CallLogDetails();
        d1.setCallLogId(1L);
        Advisor.CallLogDetails d2 = new Advisor.CallLogDetails();
        d2.setCallLogId(2L);
        Advisor.CallLogDetails d3 = new Advisor.CallLogDetails();
        d3.setCallLogId(3L);
        advisor.setCallLogDetails(List.of(d1, d2, d3));

        PaginationRequest page = new PaginationRequest(1, 1, "createdAt", "DESC");
        CallLogResponse callLogResponse = new CallLogResponse();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(callReadService.getCallLogsByIDs(List.of(2L))).thenReturn(List.of(callLogResponse));

        PaginatedResponse<AdvisorCallLogResponse> result =
                advisorCallReadService.getCallLogs(advisorIdentifier, page);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertNotNull(result.getPagination());
        assertEquals(3, result.getPagination().getTotalElements());
        assertTrue(result.getPagination().isHasNext());
        assertTrue(result.getPagination().isHasPrevious());
        verify(callReadService).getCallLogsByIDs(List.of(2L));
    }
}
