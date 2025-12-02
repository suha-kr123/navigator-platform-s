package com.nivasafinance.features.advisoractivity.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisoractivity.dto.AdvisorActivityResponse;
import com.nivasafinance.features.advisoractivity.repository.AdvisorActivityRepositoryWrapper;
import com.nivasafinance.features.advisoractivity.service.AdvisorActivityReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdvisorActivityReadServiceImpl implements AdvisorActivityReadService {

    private final AdvisorActivityRepositoryWrapper advisorActivityRepositoryWrapper;
    private final AdvisorReadService advisorReadService;

    @Override
    public PaginatedResponse<AdvisorActivityResponse> getActivities(UUID advisorIdentifier,
                                                                   PaginationRequest paginationRequest) {
        Long advisorId = advisorReadService.getAdvisorByIdentifier(advisorIdentifier).getId();
        return advisorActivityRepositoryWrapper.findAllByAdvisorIdWithException(advisorId, paginationRequest);
    }
}

