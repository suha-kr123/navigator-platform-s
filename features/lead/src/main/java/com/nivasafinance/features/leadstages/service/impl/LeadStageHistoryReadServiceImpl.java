package com.nivasafinance.features.leadstages.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class LeadStageHistoryReadServiceImpl implements LeadStageHistoryReadService {

    private final LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;

    @Override
    public PaginatedResponse<LeadStageHistoryResponse> getStageHistoryByLeadId(Long leadId, PaginationRequest paginationRequest) {
        PaginatedResponse<LeadStageHistory> paginatedResponse = 
                leadStageHistoryRepositoryWrapper.findByLeadIdOrderByEnteredAtDesc(leadId, paginationRequest);
        
        List<LeadStageHistoryResponse> responses = paginatedResponse.getContent().stream()
                .map(LeadStageHistoryResponse::from)
                .collect(Collectors.toList());
        
        return new PaginatedResponse<>(responses, paginatedResponse.getPagination());
    }
}

