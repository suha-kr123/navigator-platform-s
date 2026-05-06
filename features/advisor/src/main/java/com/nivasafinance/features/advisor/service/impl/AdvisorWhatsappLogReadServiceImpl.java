package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappLogResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorWhatsappLogReadService;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
import com.nivasafinance.features.whatsapp.entity.WhatsappLogAdvisor;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import com.nivasafinance.features.whatsapp.service.WhatsappLogReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AdvisorWhatsappLogReadServiceImpl implements AdvisorWhatsappLogReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final WhatsappLogReadService whatsappLogReadService;

    @Override
    public PaginatedResponse<AdvisorWhatsappLogResponse> getWhatsappMessages(
            UUID advisorIdentifier,
            AdvisorDashboardFilters filters,
            PaginationRequest paginationRequest
    ) {
        String filter = extractFilter(filters);
        log.info("Fetching whatsapp messages for advisorIdentifier={}, filter={}, limit={}, offset={}",
                advisorIdentifier, filter, paginationRequest.getLimit(), paginationRequest.getOffset());

        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        Long advisorId = advisor.getId();

        WhatsappCreatedSource sourceFilter = parseFilter(filter);

        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be > 0, got " + limit);
        }
        int page = offset / limit;

        try {
            Page<WhatsappLogAdvisor> mappingPage = whatsappLogReadService
                    .findAdvisorMappingsByAdvisorId(advisorId, sourceFilter, PageRequest.of(page, limit));

            List<AdvisorWhatsappLogResponse> items = List.of();
            if (!mappingPage.isEmpty()) {
                List<Long> logIds = mappingPage.getContent().stream()
                        .map(WhatsappLogAdvisor::getWhatsappLogId)
                        .collect(Collectors.toList());
                items = whatsappLogReadService.getWhatsappLogsByIds(logIds).stream()
                        .map(this::toAdvisorResponse)
                        .collect(Collectors.toList());
            }

            log.info("Returning {} whatsapp messages for advisorIdentifier={} (totalElements={})",
                    items.size(), advisorIdentifier, mappingPage.getTotalElements());

            return new PaginatedResponse<>(
                    items,
                    PaginationInfo.builder()
                            .limit(limit)
                            .offset(offset)
                            .totalElements((int) mappingPage.getTotalElements())
                            .totalPages(mappingPage.getTotalPages())
                            .currentPage(page)
                            .hasNext(mappingPage.hasNext())
                            .hasPrevious(mappingPage.hasPrevious())
                            .build()
            );
        } catch (DataAccessException e) {
            log.error("DB error fetching whatsapp messages for advisorIdentifier={}: {}",
                    advisorIdentifier, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve whatsapp messages for advisor", e);
        }
    }

    private AdvisorWhatsappLogResponse toAdvisorResponse(WhatsappLogResponse src) {
        return AdvisorWhatsappLogResponse.builder()
                .identifier(src.getIdentifier())
                .direction(src.getDirection())
                .senderLabel(src.getSenderLabel())
                .messageType(src.getMessageType())
                .messageData(src.getMessageData())
                .templateDetails(src.getTemplateDetails())
                .status(src.getStatus())
                .createdSourceType(src.getCreatedSourceType())
                .messageTime(src.getMessageTime())
                .build();
    }

    private String extractFilter(AdvisorDashboardFilters filters) {
        if (filters == null || filters.getStatus() == null || filters.getStatus().isEmpty()) {
            return null;
        }
        return filters.getStatus().get(0);
    }

    private WhatsappCreatedSource parseFilter(String filter) {
        if (filter == null || filter.isBlank() || "ALL".equalsIgnoreCase(filter)) {
            return null;
        }
        return switch (filter.toUpperCase()) {
            case "NOTIFICATION" -> WhatsappCreatedSource.API;
            case "SEQUENCE" -> WhatsappCreatedSource.SEQUENCE;
            case "BOT_TRIGGERED" -> WhatsappCreatedSource.BOT;
            default -> {
                log.warn("Unknown whatsapp filter '{}', falling back to ALL", filter);
                yield null;
            }
        };
    }
}
