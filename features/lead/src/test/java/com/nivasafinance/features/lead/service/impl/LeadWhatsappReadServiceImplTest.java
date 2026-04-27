package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadWhatsappNotificationResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepositoryWrapper;
import com.nivasafinance.notification.orchestrator.service.WhatsAppNotificationReadService;
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
class LeadWhatsappReadServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private LeadWhatsAppNotificationRepositoryWrapper leadWhatsAppNotificationRepositoryWrapper;

    @Mock
    private WhatsAppNotificationReadService whatsAppNotificationReadService;

    @InjectMocks
    private LeadWhatsappReadServiceImpl leadWhatsappReadService;

    private UUID leadIdentifier;
    private Lead lead;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        lead = new Lead();
        lead.setId(1L);
        lead.setLeadIdentifier(leadIdentifier);
        paginationRequest = new PaginationRequest(0, 10, "createdAt", "DESC");
    }

    @Test
    void getWhatsappNotifications_success_returnsPaginatedResponseWithLeadIdentifier() {
        UUID receiptId = UUID.randomUUID();
        LeadWhatsAppNotification row = LeadWhatsAppNotification.builder()
                .id(42L)
                .leadIdentifier(leadIdentifier)
                .receiptId(receiptId)
                .templateName("welcome_tpl")
                .build();
        row.setCreatedAt(LocalDateTime.now());

        Page<LeadWhatsAppNotification> page = new PageImpl<>(List.of(row), PageRequest.of(0, 10), 1);
        WhatsAppNotificationLogResponse logDto = WhatsAppNotificationLogResponse.builder()
                .id(42L)
                .templateName("welcome_tpl")
                .provider("GALLABOX")
                .toNumber("919999999999")
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadWhatsAppNotificationRepositoryWrapper.findByLeadIdentifierOrderByIdDesc(eq(leadIdentifier), any(PageRequest.class)))
                .thenReturn(page);
        when(whatsAppNotificationReadService.getLeadWhatsAppNotificationLogsByIds(List.of(42L)))
                .thenReturn(List.of(logDto));

        PaginatedResponse<LeadWhatsappNotificationResponse> result =
                leadWhatsappReadService.getWhatsappNotifications(leadIdentifier, paginationRequest);

        assertNotNull(result, "Paginated response should not be null");
        assertEquals(1, result.getContent().size(), "Should return one WhatsApp row");
        assertEquals(leadIdentifier, result.getContent().get(0).getLeadIdentifier(),
                "Each row should echo the path lead identifier");
        assertNotNull(result.getContent().get(0).getWhatsappLogDetails(), "Log details should be populated");
        assertEquals(42L, result.getContent().get(0).getWhatsappLogDetails().getId(), "Log id should match repository row");
        assertEquals("welcome_tpl", result.getContent().get(0).getWhatsappLogDetails().getTemplateName(),
                "Template name should come from read service DTO");
        assertEquals(1, result.getPagination().getTotalElements(), "Total count should come from page");
        verify(whatsAppNotificationReadService).getLeadWhatsAppNotificationLogsByIds(List.of(42L));
    }

    @Test
    void getWhatsappNotifications_emptyPage_returnsEmptyContentAndZeroTotals() {
        Page<LeadWhatsAppNotification> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadWhatsAppNotificationRepositoryWrapper.findByLeadIdentifierOrderByIdDesc(eq(leadIdentifier), any(PageRequest.class)))
                .thenReturn(emptyPage);

        PaginatedResponse<LeadWhatsappNotificationResponse> result =
                leadWhatsappReadService.getWhatsappNotifications(leadIdentifier, paginationRequest);

        assertNotNull(result, "Paginated response should not be null");
        assertTrue(result.getContent().isEmpty(), "Content should be empty when page has no rows");
        assertEquals(0, result.getPagination().getTotalElements(), "Total elements should be zero");
        verify(whatsAppNotificationReadService, never()).getLeadWhatsAppNotificationLogsByIds(any());
    }

    @Test
    void getWhatsappNotifications_pagination_passesCorrectPageRequest() {
        paginationRequest.setLimit(5);
        paginationRequest.setOffset(10);

        Page<LeadWhatsAppNotification> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 5), 0);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadWhatsAppNotificationRepositoryWrapper.findByLeadIdentifierOrderByIdDesc(eq(leadIdentifier), eq(PageRequest.of(2, 5))))
                .thenReturn(emptyPage);

        leadWhatsappReadService.getWhatsappNotifications(leadIdentifier, paginationRequest);

        verify(leadWhatsAppNotificationRepositoryWrapper).findByLeadIdentifierOrderByIdDesc(eq(leadIdentifier), eq(PageRequest.of(2, 5)));
    }

    @Test
    void getWhatsappNotifications_leadNotFound_propagatesException() {
        doThrow(new RuntimeException("Lead not found"))
                .when(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);

        assertThrows(RuntimeException.class,
                () -> leadWhatsappReadService.getWhatsappNotifications(leadIdentifier, paginationRequest),
                "Should propagate when lead lookup fails");
        verifyNoInteractions(leadWhatsAppNotificationRepositoryWrapper);
        verifyNoInteractions(whatsAppNotificationReadService);
    }
}
