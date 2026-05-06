package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappLogResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import com.nivasafinance.features.whatsapp.service.WhatsappLogReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private AdvisorDashboardFilters allFilters;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(7L);
        advisor.setIdentifier(advisorIdentifier);
        paginationRequest = new PaginationRequest(0, 10, "createdAt", "DESC");
        allFilters = new AdvisorDashboardFilters();
    }

    @Test
    void getWhatsappMessages_success_returnsPaginatedResponseWithMappedItems() {
        WhatsappLogAdvisor mapping = new WhatsappLogAdvisor(55L, 7L);
        Page<WhatsappLogAdvisor> mappingPage = new PageImpl<>(List.of(mapping), PageRequest.of(0, 10), 1);

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
        when(whatsappLogReadService.findAdvisorMappingsByAdvisorId(eq(7L), isNull(), any(PageRequest.class)))
                .thenReturn(mappingPage);
        when(whatsappLogReadService.getWhatsappLogsByIds(List.of(55L)))
                .thenReturn(List.of(logResponse));

        PaginatedResponse<AdvisorWhatsappLogResponse> result =
                advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, allFilters, paginationRequest);

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
        Page<WhatsappLogAdvisor> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorMappingsByAdvisorId(eq(7L), isNull(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        PaginatedResponse<AdvisorWhatsappLogResponse> result =
                advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, allFilters, paginationRequest);

        assertTrue(result.getContent().isEmpty());
        verify(whatsappLogReadService, never()).getWhatsappLogsByIds(any());
    }

    @Test
    void getWhatsappMessages_advisorNotFound_propagatesException() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier))
                .thenThrow(new RuntimeException("Advisor not found"));

        assertThrows(RuntimeException.class,
                () -> advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, allFilters, paginationRequest));
        verifyNoInteractions(whatsappLogReadService);
    }

    @Test
    void getWhatsappMessages_filterNotification_passesApiSource() {
        AdvisorDashboardFilters filters = AdvisorDashboardFilters.builder()
                .status(List.of("NOTIFICATION"))
                .build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorMappingsByAdvisorId(eq(7L), eq(WhatsappCreatedSource.API), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findAdvisorMappingsByAdvisorId(eq(7L), eq(WhatsappCreatedSource.API), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_filterSequence_passesSequenceSource() {
        AdvisorDashboardFilters filters = AdvisorDashboardFilters.builder()
                .status(List.of("SEQUENCE"))
                .build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorMappingsByAdvisorId(eq(7L), eq(WhatsappCreatedSource.SEQUENCE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findAdvisorMappingsByAdvisorId(eq(7L), eq(WhatsappCreatedSource.SEQUENCE), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_filterBotTriggered_passesBotSource() {
        AdvisorDashboardFilters filters = AdvisorDashboardFilters.builder()
                .status(List.of("BOT_TRIGGERED"))
                .build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorMappingsByAdvisorId(eq(7L), eq(WhatsappCreatedSource.BOT), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findAdvisorMappingsByAdvisorId(eq(7L), eq(WhatsappCreatedSource.BOT), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_unknownFilter_treatsAsAll() {
        AdvisorDashboardFilters filters = AdvisorDashboardFilters.builder()
                .status(List.of("GIBBERISH"))
                .build();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(whatsappLogReadService.findAdvisorMappingsByAdvisorId(eq(7L), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        advisorWhatsappLogReadService.getWhatsappMessages(advisorIdentifier, filters, paginationRequest);

        ArgumentCaptor<WhatsappCreatedSource> captor = ArgumentCaptor.forClass(WhatsappCreatedSource.class);
        verify(whatsappLogReadService).findAdvisorMappingsByAdvisorId(eq(7L), captor.capture(), any(PageRequest.class));
        assertNull(captor.getValue());
    }
}
