package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationUtils;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.lead.service.LeadCallReadService;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.lead.dto.LeadCallLogResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LeadCallReadServiceImpl implements LeadCallReadService {

    private final CallReadService callReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;

    @Override
    public PaginatedResponse<LeadCallLogResponse> getCallLogs(UUID leadIdentifier, PaginationRequest paginationRequest) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        List<Lead.CallLogDetails> callLogDetails = lead.getCallLogDetails();
        if (callLogDetails == null || callLogDetails.isEmpty()) {
            return PaginationUtils.toBasicPaginatedResponse(List.of());
        }
        // Get
        List<Long> ids = callLogDetails.stream()
                .sorted(Comparator.comparing(Lead.CallLogDetails::getCallLogId).reversed())
                .skip(paginationRequest.getOffset())
                .limit(paginationRequest.getLimit())
                .map(Lead.CallLogDetails::getCallLogId)
                .collect(Collectors.toList());

        List<CallLogResponse> callLogResponses = callReadService.getCallLogsByIDs(ids);
        List<LeadCallLogResponse> leadCallLogResponses = callLogResponses.stream()
                .map(LeadCallLogResponse::new)
                .collect(Collectors.toList());
        return new PaginatedResponse<>(
                leadCallLogResponses,
                PaginationInfo.builder()
                        .limit(paginationRequest.getLimit())
                        .offset(paginationRequest.getOffset())
                        .totalElements(callLogResponses.size())
                        .totalPages(callLogResponses.size() / paginationRequest.getLimit())
                        .currentPage(paginationRequest.getOffset() / paginationRequest.getLimit())
                        .hasNext(paginationRequest.getOffset() + paginationRequest.getLimit() < callLogResponses.size())
                        .hasPrevious(paginationRequest.getOffset() > 0)
                        .build()
        );
    }
}
