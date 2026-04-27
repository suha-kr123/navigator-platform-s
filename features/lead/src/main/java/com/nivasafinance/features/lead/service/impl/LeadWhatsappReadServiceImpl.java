package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadWhatsappNotificationResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadWhatsappReadService;
import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepositoryWrapper;
import com.nivasafinance.notification.orchestrator.service.WhatsAppNotificationReadService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeadWhatsappReadServiceImpl implements LeadWhatsappReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadWhatsAppNotificationRepositoryWrapper leadWhatsAppNotificationRepositoryWrapper;
    private final WhatsAppNotificationReadService whatsAppNotificationReadService;

    @Override
    public PaginatedResponse<LeadWhatsappNotificationResponse> getWhatsappNotifications(
            UUID leadIdentifier,
            PaginationRequest paginationRequest
    ) {
        leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int page = offset / limit;

        Page<LeadWhatsAppNotification> mappingPage = leadWhatsAppNotificationRepositoryWrapper
                .findByLeadIdentifierOrderByIdDesc(leadIdentifier, PageRequest.of(page, limit));

        if (mappingPage.isEmpty()) {
            return new PaginatedResponse<>(
                    List.of(),
                    PaginationInfo.builder()
                            .limit(limit)
                            .offset(offset)
                            .totalElements(0)
                            .totalPages(0)
                            .currentPage(0)
                            .hasNext(false)
                            .hasPrevious(false)
                            .build()
            );
        }

        List<Long> pageIds = mappingPage.getContent().stream()
                .map(LeadWhatsAppNotification::getId)
                .collect(Collectors.toList());

        List<WhatsAppNotificationLogResponse> logResponses =
                whatsAppNotificationReadService.getLeadWhatsAppNotificationLogsByIds(pageIds);
        Map<Long, WhatsAppNotificationLogResponse> byId = logResponses.stream()
                .collect(Collectors.toMap(WhatsAppNotificationLogResponse::getId, Function.identity(), (a, b) -> a));

        List<LeadWhatsappNotificationResponse> leadWhatsappNotificationResponses = mappingPage.getContent().stream()
                .map(n -> {
                    WhatsAppNotificationLogResponse detail = byId.get(n.getId());
                    return LeadWhatsappNotificationResponse.builder()
                            .whatsappLogDetails(detail)
                            .leadIdentifier(leadIdentifier)
                            .build();
                })
                .collect(Collectors.toList());

        long total = mappingPage.getTotalElements();
        int totalPages = mappingPage.getTotalPages();
        return new PaginatedResponse<>(
                leadWhatsappNotificationResponses,
                PaginationInfo.builder()
                        .limit(limit)
                        .offset(offset)
                        .totalElements((int) total)
                        .totalPages(totalPages)
                        .currentPage(page)
                        .hasNext(mappingPage.hasNext())
                        .hasPrevious(mappingPage.hasPrevious())
                        .build()
        );
    }
}
