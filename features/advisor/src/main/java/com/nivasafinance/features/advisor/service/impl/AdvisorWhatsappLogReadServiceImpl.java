package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorWhatsappLogResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorWhatsappLogReadService;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogFilters;
import com.nivasafinance.features.whatsapp.dto.WhatsappLogResponse;
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
            WhatsappLogFilters filters,
            PaginationRequest paginationRequest
    ) {
        WhatsappLogFilters effectiveFilters = filters != null ? filters : new WhatsappLogFilters();
        log.info("Fetching whatsapp messages for advisorIdentifier={}, filters={}, limit={}, offset={}",
                advisorIdentifier, effectiveFilters,
                paginationRequest.getLimit(), paginationRequest.getOffset());

        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        Long advisorId = advisor.getId();

        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be > 0, got " + limit);
        }
        int page = offset / limit;

        try {
            Page<Long> idPage = whatsappLogReadService
                    .findAdvisorWhatsappLogIds(advisorId, effectiveFilters, PageRequest.of(page, limit));

            List<AdvisorWhatsappLogResponse> items = List.of();
            if (!idPage.isEmpty()) {
                items = whatsappLogReadService.getWhatsappLogsByIds(idPage.getContent()).stream()
                        .map(this::toAdvisorResponse)
                        .collect(Collectors.toList());
            }

            log.info("Returning {} whatsapp messages for advisorIdentifier={} (totalElements={})",
                    items.size(), advisorIdentifier, idPage.getTotalElements());

            return new PaginatedResponse<>(
                    items,
                    PaginationInfo.builder()
                            .limit(limit)
                            .offset(offset)
                            .totalElements((int) idPage.getTotalElements())
                            .totalPages(idPage.getTotalPages())
                            .currentPage(page)
                            .hasNext(idPage.hasNext())
                            .hasPrevious(idPage.hasPrevious())
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
}
