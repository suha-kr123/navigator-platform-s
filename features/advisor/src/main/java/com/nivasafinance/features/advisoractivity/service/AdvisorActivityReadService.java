package com.nivasafinance.features.advisoractivity.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisoractivity.dto.AdvisorActivityResponse;

import java.util.UUID;

public interface AdvisorActivityReadService {

    PaginatedResponse<AdvisorActivityResponse> getActivities(UUID advisorIdentifier, PaginationRequest paginationRequest);
}

