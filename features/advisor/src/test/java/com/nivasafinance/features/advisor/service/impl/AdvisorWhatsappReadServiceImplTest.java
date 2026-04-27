package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappNotificationResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepositoryWrapper;
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
class AdvisorWhatsappReadServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private AdvisorWhatsAppNotificationRepositoryWrapper advisorWhatsAppNotificationRepositoryWrapper;

    @Mock
    private WhatsAppNotificationReadService whatsAppNotificationReadService;

    @InjectMocks
    private AdvisorWhatsappReadServiceImpl advisorWhatsappReadService;

    private UUID advisorIdentifier;
    private Advisor advisor;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        paginationRequest = new PaginationRequest(0, 10, "createdAt", "DESC");
    }

    @Test
    void getWhatsappNotifications_success_returnsPaginatedResponseWithAdvisorIdentifier() {
        UUID receiptId = UUID.randomUUID();
        AdvisorWhatsAppNotification row = AdvisorWhatsAppNotification.builder()
                .id(7L)
                .advisorIdentifier(advisorIdentifier)
                .receiptId(receiptId)
                .templateName("advisor_tpl")
                .build();
        row.setCreatedAt(LocalDateTime.now());

        Page<AdvisorWhatsAppNotification> page = new PageImpl<>(List.of(row), PageRequest.of(0, 10), 1);
        WhatsAppNotificationLogResponse logDto = WhatsAppNotificationLogResponse.builder()
                .id(7L)
                .templateName("advisor_tpl")
                .provider("GALLABOX")
                .toNumber("918888888888")
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorWhatsAppNotificationRepositoryWrapper.findByAdvisorIdentifierOrderByIdDesc(eq(advisorIdentifier), any(PageRequest.class)))
                .thenReturn(page);
        when(whatsAppNotificationReadService.getAdvisorWhatsAppNotificationLogsByIds(List.of(7L)))
                .thenReturn(List.of(logDto));

        PaginatedResponse<AdvisorWhatsappNotificationResponse> result =
                advisorWhatsappReadService.getWhatsappNotifications(advisorIdentifier, paginationRequest);

        assertNotNull(result, "Paginated response should not be null");
        assertEquals(1, result.getContent().size(), "Should return one WhatsApp row");
        assertEquals(advisorIdentifier, result.getContent().get(0).getAdvisorIdentifier(),
                "Each row should echo the path advisor identifier");
        assertNotNull(result.getContent().get(0).getWhatsappLogDetails(), "Log details should be populated");
        assertEquals(7L, result.getContent().get(0).getWhatsappLogDetails().getId(), "Log id should match repository row");
        verify(whatsAppNotificationReadService).getAdvisorWhatsAppNotificationLogsByIds(List.of(7L));
    }

    @Test
    void getWhatsappNotifications_emptyPage_returnsEmptyContent() {
        Page<AdvisorWhatsAppNotification> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorWhatsAppNotificationRepositoryWrapper.findByAdvisorIdentifierOrderByIdDesc(eq(advisorIdentifier), any(PageRequest.class)))
                .thenReturn(emptyPage);

        PaginatedResponse<AdvisorWhatsappNotificationResponse> result =
                advisorWhatsappReadService.getWhatsappNotifications(advisorIdentifier, paginationRequest);

        assertTrue(result.getContent().isEmpty(), "Content should be empty when page has no rows");
        verify(whatsAppNotificationReadService, never()).getAdvisorWhatsAppNotificationLogsByIds(any());
    }

    @Test
    void getWhatsappNotifications_advisorNotFound_propagatesException() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier))
                .thenThrow(new RuntimeException("Advisor not found"));

        assertThrows(RuntimeException.class,
                () -> advisorWhatsappReadService.getWhatsappNotifications(advisorIdentifier, paginationRequest),
                "Should propagate when advisor lookup fails");
        verifyNoInteractions(advisorWhatsAppNotificationRepositoryWrapper);
        verifyNoInteractions(whatsAppNotificationReadService);
    }
}
