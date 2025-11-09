package com.nivasafinance.features.leadactivity.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.leadactivity.dto.LeadActivityResponse;
import com.nivasafinance.features.leadactivity.repository.LeadActivityRepositoryWrapper;
import com.nivasafinance.features.leadactivity.service.LeadActivityReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class LeadActivityReadServiceImpl implements LeadActivityReadService {

    private final LeadActivityRepositoryWrapper leadActivityRepositoryWrapper;
    private final LeadReadService leadReadService;

    @Override
    public PaginatedResponse<LeadActivityResponse> getActivities(UUID leadIdentifier,
                                                                 PaginationRequest paginationRequest) {
        Long leadId = leadReadService.getLeadBasicByIdentifier(leadIdentifier).getId();
        return leadActivityRepositoryWrapper.findAllByLeadIdentifierWithException(leadId, paginationRequest);
    }
}


