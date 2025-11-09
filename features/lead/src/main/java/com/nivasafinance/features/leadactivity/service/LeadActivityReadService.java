package com.nivasafinance.features.leadactivity.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadactivity.dto.LeadActivityResponse;

import java.util.UUID;

public interface LeadActivityReadService {

    PaginatedResponse<LeadActivityResponse> getActivities(UUID leadIdentifier, PaginationRequest paginationRequest);
}


