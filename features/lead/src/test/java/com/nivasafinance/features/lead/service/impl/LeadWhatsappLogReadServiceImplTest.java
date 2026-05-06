package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadWhatsappLogResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogLead;
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
class LeadWhatsappLogReadServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private WhatsappLogReadService whatsappLogReadService;

    @InjectMocks
    private LeadWhatsappLogReadServiceImpl leadWhatsappLogReadService;

    private UUID leadIdentifier;
    private Lead lead;
    private PaginationRequest paginationRequest;
    private LeadDashboardFilters allFilters;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        lead = new Lead();
        lead.setId(11L);
        lead.setLeadIdentifier(leadIdentifier);
        paginationRequest = new PaginationRequest(0, 10, "createdAt", "DESC");
        allFilters = new LeadDashboardFilters();
    }

    @Test
    void getWhatsappMessages_success_returnsPaginatedResponseWithMappedItems() {
        WhatsappLogLead mapping = new WhatsappLogLead(42L, 11L, 99L);
        Page<WhatsappLogLead> mappingPage = new PageImpl<>(List.of(mapping), PageRequest.of(0, 10), 1);

        UUID logIdentifier = UUID.randomUUID();
        WhatsappLogResponse logResponse = WhatsappLogResponse.builder()
                .id(42L)
                .identifier(logIdentifier)
                .direction("RECEIVED")
                .senderLabel("Customer")
                .messageType("text")
                .status("DELIVERED")
                .messageTime(LocalDateTime.now())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(whatsappLogReadService.findLeadMappingsByLeadId(eq(11L), isNull(), any(PageRequest.class)))
                .thenReturn(mappingPage);
        when(whatsappLogReadService.getWhatsappLogsByIds(List.of(42L)))
                .thenReturn(List.of(logResponse));

        PaginatedResponse<LeadWhatsappLogResponse> result =
                leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, allFilters, paginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        LeadWhatsappLogResponse item = result.getContent().get(0);
        assertEquals(logIdentifier, item.getIdentifier());
        assertEquals("RECEIVED", item.getDirection());
        assertEquals("Customer", item.getSenderLabel());
        assertEquals("text", item.getMessageType());
        assertEquals("DELIVERED", item.getStatus());
    }

    @Test
    void getWhatsappMessages_emptyPage_returnsEmptyContentAndDoesNotFetchLogs() {
        Page<WhatsappLogLead> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(whatsappLogReadService.findLeadMappingsByLeadId(eq(11L), isNull(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        PaginatedResponse<LeadWhatsappLogResponse> result =
                leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, allFilters, paginationRequest);

        assertTrue(result.getContent().isEmpty());
        verify(whatsappLogReadService, never()).getWhatsappLogsByIds(any());
    }

    @Test
    void getWhatsappMessages_leadNotFound_propagatesException() {
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        assertThrows(RuntimeException.class,
                () -> leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, allFilters, paginationRequest));
        verifyNoInteractions(whatsappLogReadService);
    }

    @Test
    void getWhatsappMessages_filterNotification_passesApiSource() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder()
                .status(List.of("NOTIFICATION"))
                .build();
        Page<WhatsappLogLead> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(whatsappLogReadService.findLeadMappingsByLeadId(eq(11L), eq(WhatsappCreatedSource.API), any(PageRequest.class)))
                .thenReturn(emptyPage);

        leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findLeadMappingsByLeadId(eq(11L), eq(WhatsappCreatedSource.API), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_filterSequence_passesSequenceSource() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder()
                .status(List.of("SEQUENCE"))
                .build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(whatsappLogReadService.findLeadMappingsByLeadId(eq(11L), eq(WhatsappCreatedSource.SEQUENCE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findLeadMappingsByLeadId(eq(11L), eq(WhatsappCreatedSource.SEQUENCE), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_filterBotTriggered_passesBotSource() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder()
                .status(List.of("BOT_TRIGGERED"))
                .build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(whatsappLogReadService.findLeadMappingsByLeadId(eq(11L), eq(WhatsappCreatedSource.BOT), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, filters, paginationRequest);

        verify(whatsappLogReadService).findLeadMappingsByLeadId(eq(11L), eq(WhatsappCreatedSource.BOT), any(PageRequest.class));
    }

    @Test
    void getWhatsappMessages_unknownFilter_treatsAsAll() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder()
                .status(List.of("GIBBERISH"))
                .build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(whatsappLogReadService.findLeadMappingsByLeadId(eq(11L), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        leadWhatsappLogReadService.getWhatsappMessages(leadIdentifier, filters, paginationRequest);

        ArgumentCaptor<WhatsappCreatedSource> captor = ArgumentCaptor.forClass(WhatsappCreatedSource.class);
        verify(whatsappLogReadService).findLeadMappingsByLeadId(eq(11L), captor.capture(), any(PageRequest.class));
        assertNull(captor.getValue());
    }
}
