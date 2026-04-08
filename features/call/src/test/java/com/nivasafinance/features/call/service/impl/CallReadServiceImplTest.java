package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.call.repository.CallLogRepository;
import com.nivasafinance.features.call.repository.CallLogRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class CallReadServiceImplTest {

    @Mock
    private CallLogRepositoryWrapper callLogRepositoryWrapper;
    @Mock
    private CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;
    @Mock
    private CallLogRepository callLogRepository;

    @InjectMocks
    private CallReadServiceImpl service;

    @Test
    void getCallLogByIdentifier_returnsMappedResponse() {
        CallLog callLog = sampleCallLog(1L, "provider-1");
        when(callLogRepositoryWrapper.findByIdentifierWithException(callLog.getIdentifier()))
                .thenReturn(callLog);

        CallLogResponse response = service.getCallLogByIdentifier(callLog.getIdentifier());

        assertEquals(callLog.getId(), response.getId());
        assertEquals(callLog.getProviderId(), response.getProviderId());
        verify(callLogRepositoryWrapper).findByIdentifierWithException(callLog.getIdentifier());
    }

    @Test
    void getCallLogsByIDs_returnsMappedList() {
        CallLog c1 = sampleCallLog(1L, "p1");
        CallLog c2 = sampleCallLog(2L, "p2");
        when(callLogRepositoryWrapper.findByIdsWithException(List.of(1L, 2L)))
                .thenReturn(List.of(c1, c2));

        List<CallLogResponse> responses = service.getCallLogsByIDs(List.of(1L, 2L));

        assertEquals(2, responses.size());
        assertEquals("p1", responses.get(0).getProviderId());
    }

    @Test
    void getCallLogByID_returnsMappedResponse() {
        CallLog callLog = sampleCallLog(5L, "provider-5");
        when(callLogRepositoryWrapper.findByIdWithException(5L)).thenReturn(callLog);

        CallLogResponse response = service.getCallLogByID(5L);

        assertEquals("provider-5", response.getProviderId());
        verify(callLogRepositoryWrapper).findByIdWithException(5L);
    }

    @Test
    void getCallLogByProviderId_presentOptional() {
        CallLog callLog = sampleCallLog(10L, "pr-10");
        when(callLogRepository.findByProviderId("pr-10")).thenReturn(Optional.of(callLog));

        Optional<CallLogResponse> result = service.getCallLogByProviderId("pr-10");

        assertTrue(result.isPresent());
        assertEquals("pr-10", result.get().getProviderId());
    }

    @Test
    void getCallLogByProviderId_emptyOptional() {
        when(callLogRepository.findByProviderId("missing")).thenReturn(Optional.empty());

        Optional<CallLogResponse> result = service.getCallLogByProviderId("missing");

        assertTrue(result.isEmpty());
    }

    @Test
    void findLeadIdByCallLogId_returnsLeadId() {
        var mapping = new com.nivasafinance.features.call.entity.CallLogLead(20L, 99L, null);
        when(callLogLeadRepositoryWrapper.findByCallLogId(20L)).thenReturn(Optional.of(mapping));

        Optional<Long> result = service.findLeadIdByCallLogId(20L);

        assertTrue(result.isPresent());
        assertEquals(99L, result.get());
    }

    @Test
    void findByProviderAndCreatedAtRange_delegatesToRepository() {
        CallLog callLog = sampleCallLog(3L, "provider-3");
        Page<CallLog> page = new PageImpl<>(List.of(callLog));
        when(callLogRepository.findByProviderAndCreatedAtRange(
                any(), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        Page<CallLog> result = service.findByProviderAndCreatedAtRange(
                CallProvider.EXOTEL,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        verify(callLogRepository).findByProviderAndCreatedAtRange(any(), any(), any(), any(PageRequest.class));
    }

    private CallLog sampleCallLog(Long id, String providerId) {
        CallLog callLog = new CallLog();
        callLog.setId(id);
        callLog.setIdentifier(UUID.randomUUID());
        callLog.setProvider(CallProvider.EXOTEL);
        callLog.setProviderId(providerId);
        callLog.setCallerId("caller");
        callLog.setFromNumber("from");
        callLog.setToNumber("to");
        callLog.setDirection(CallDirection.OUTBOUND);
        callLog.setSource(CallSource.CRM);
        callLog.setStatus(CallStatus.COMPLETED);
        callLog.setCreatedAt(LocalDateTime.now());
        return callLog;
    }
}
