package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.repository.CallLogLeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadCallReadService;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.lead.dto.LeadCallLogResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LeadCallReadServiceImpl implements LeadCallReadService {

    private final CallReadService callReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final CallLogLeadRepositoryWrapper callLogLeadRepositoryWrapper;

    @Override
    public PaginatedResponse<LeadCallLogResponse> getCallLogs(UUID leadIdentifier, PaginationRequest paginationRequest) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int page = offset / limit;

        Page<CallLogLead> mappingPage = callLogLeadRepositoryWrapper.findByLeadId(
                lead.getId(), PageRequest.of(page, limit));

        if (mappingPage.isEmpty()) {
            return new PaginatedResponse<>(
                    List.of(),
                    PaginationInfo.builder()
                            .limit(limit).offset(offset)
                            .totalElements(0).totalPages(0).currentPage(0)
                            .hasNext(false).hasPrevious(false)
                            .build());
        }

        List<Long> ids = mappingPage.getContent().stream()
                .map(CallLogLead::getCallLogId)
                .collect(Collectors.toList());

        List<CallLogResponse> callLogResponses = callReadService.getCallLogsByIDs(ids);
        List<LeadCallLogResponse> leadCallLogResponses = callLogResponses.stream()
                .map(LeadCallLogResponse::new)
                .collect(Collectors.toList());

        long total = mappingPage.getTotalElements();
        int totalPages = mappingPage.getTotalPages();
        return new PaginatedResponse<>(
                leadCallLogResponses,
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
