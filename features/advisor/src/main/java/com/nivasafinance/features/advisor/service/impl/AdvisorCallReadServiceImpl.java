package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationUtils;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.advisor.service.AdvisorCallReadService;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.advisor.dto.AdvisorCallLogResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
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
public class AdvisorCallReadServiceImpl implements AdvisorCallReadService {

    private final CallReadService callReadService;
    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Override
    public PaginatedResponse<AdvisorCallLogResponse> getCallLogs(UUID advisorIdentifier, PaginationRequest paginationRequest) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
        List<Advisor.CallLogDetails> callLogDetails = advisor.getCallLogDetails();
        if (callLogDetails == null || callLogDetails.isEmpty()) {
            return PaginationUtils.toBasicPaginatedResponse(List.of());
        }
        // Get call log IDs, sorted by callLogId descending, with pagination
        List<Long> ids = callLogDetails.stream()
                .sorted(Comparator.comparing(Advisor.CallLogDetails::getCallLogId).reversed())
                .skip(paginationRequest.getOffset())
                .limit(paginationRequest.getLimit())
                .map(Advisor.CallLogDetails::getCallLogId)
                .collect(Collectors.toList());

        List<CallLogResponse> callLogResponses = callReadService.getCallLogsByIDs(ids);
        List<AdvisorCallLogResponse> advisorCallLogResponses = callLogResponses.stream()
                .map(AdvisorCallLogResponse::new)
                .collect(Collectors.toList());
        return new PaginatedResponse<>(
                advisorCallLogResponses,
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
