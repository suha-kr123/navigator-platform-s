package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappNotificationResponse;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorWhatsappReadService;
import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepositoryWrapper;
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
public class AdvisorWhatsappReadServiceImpl implements AdvisorWhatsappReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final AdvisorWhatsAppNotificationRepositoryWrapper advisorWhatsAppNotificationRepositoryWrapper;
    private final WhatsAppNotificationReadService whatsAppNotificationReadService;

    @Override
    public PaginatedResponse<AdvisorWhatsappNotificationResponse> getWhatsappNotifications(
            UUID advisorIdentifier,
            PaginationRequest paginationRequest
    ) {
        advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);

        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int page = offset / limit;

        Page<AdvisorWhatsAppNotification> mappingPage = advisorWhatsAppNotificationRepositoryWrapper
                .findByAdvisorIdentifierOrderByIdDesc(advisorIdentifier, PageRequest.of(page, limit));

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
                .map(AdvisorWhatsAppNotification::getId)
                .collect(Collectors.toList());

        List<WhatsAppNotificationLogResponse> logResponses =
                whatsAppNotificationReadService.getAdvisorWhatsAppNotificationLogsByIds(pageIds);
        Map<Long, WhatsAppNotificationLogResponse> byId = logResponses.stream()
                .collect(Collectors.toMap(WhatsAppNotificationLogResponse::getId, Function.identity(), (a, b) -> a));

        List<AdvisorWhatsappNotificationResponse> advisorWhatsappNotificationResponses = mappingPage.getContent().stream()
                .map(n -> {
                    WhatsAppNotificationLogResponse detail = byId.get(n.getId());
                    return AdvisorWhatsappNotificationResponse.builder()
                            .whatsappLogDetails(detail)
                            .advisorIdentifier(advisorIdentifier)
                            .build();
                })
                .collect(Collectors.toList());

        long total = mappingPage.getTotalElements();
        int totalPages = mappingPage.getTotalPages();
        return new PaginatedResponse<>(
                advisorWhatsappNotificationResponses,
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
