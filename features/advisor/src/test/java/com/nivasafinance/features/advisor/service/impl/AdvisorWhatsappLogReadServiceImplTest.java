package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappLogResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogFilters;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import com.nivasafinance.features.whatsapp.service.WhatsappLogReadService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorWhatsappLogReadServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private WhatsappLogReadService whatsappLogReadService;

    @InjectMocks
    private AdvisorWhatsappLogReadServiceImpl advisorWhatsappLogReadService;

    private UUID advisorIdentifier;
    private Advisor advisor;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(7L);
        advisor.setIdentifier(advisorIdentifier);
        paginationRequest = new PaginationRequest(0, 10, "createdAt", "DESC");
    }

    @Test
    void getWhatsappMessages_success_returnsPaginatedResponseWithMappedItems() {
        WhatsappLogFilters filters = new WhatsappLogFilters();
        Page<Long> idPage = new PageImpl<>(List.of(55L), PageRequest.of(0, 10), 1);

        UUID logIdentifier = UUID.randomUUID();
        WhatsappLogResponse logResponse = WhatsappLogResponse.builder()
                .id(55L)
                .identifier(logIdentifier)
                .direction("SENT")
                .senderLabel("Nivasa Finance")
                .messageType("template")
                .status("SENT")
                .createdSourceType("API")
                .messageTime(LocalDateTime.now())
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorWhatsappLogIds(eq(7L), eq(filters), any(PageRequest.class)))
                .thenReturn(idPage);
        when(whatsappLogReadService.getWhatsappLogsByIds(List.of(55L)))
                .thenReturn(List.of(logResponse));

        PaginatedResponse<AdvisorWhatsappLogResponse> result =
                advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        AdvisorWhatsappLogResponse item = result.getContent().get(0);
        assertEquals(logIdentifier, item.getIdentifier());
        assertEquals("SENT", item.getDirection());
        assertEquals("Nivasa Finance", item.getSenderLabel());
        assertEquals("template", item.getMessageType());
        assertEquals("API", item.getCreatedSourceType());
    }

    @Test
    void getWhatsappMessages_emptyPage_returnsEmptyContentAndDoesNotFetchLogs() {
        WhatsappLogFilters filters = new WhatsappLogFilters();
        Page<Long> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorWhatsappLogIds(eq(7L), eq(filters), any(PageRequest.class)))
                .thenReturn(emptyPage);

        PaginatedResponse<AdvisorWhatsappLogResponse> result =
                advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        assertTrue(result.getContent().isEmpty());
        verify(whatsappLogReadService, never()).getWhatsappLogsByIds(any());
    }

    @Test
    void getWhatsappMessages_advisorNotFound_propagatesException() {
        WhatsappLogFilters filters = new WhatsappLogFilters();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier))
                .thenThrow(new RuntimeException("Advisor not found"));

        assertThrows(RuntimeException.class,
                () -> advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest));
        verifyNoInteractions(whatsappLogReadService);
    }

    @Test
    void getWhatsappMessages_messageTypeFilter_passesFiltersThrough() {
        WhatsappLogFilters filters = WhatsappLogFilters.builder()
                .messageType(List.of("TEMPLATE"))
                .sentBy(List.of("API"))
                .build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorWhatsappLogIds(eq(7L), eq(filters), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findAdvisorWhatsappLogIds(eq(7L), eq(filters), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_nullFilters_defaultsToEmptyFilters() {
        WhatsappLogFilters expected = new WhatsappLogFilters();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorWhatsappLogIds(eq(7L), eq(expected), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, null, paginationRequest);

        verify(whatsappLogReadService).findAdvisorWhatsappLogIds(eq(7L), eq(expected), any(PageRequest.class));
    }
}
