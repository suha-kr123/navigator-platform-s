package com.nivasafinance.features.admin.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdminAdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;

import java.util.UUID;

public interface AdminAdvisorService {
    void deleteAdvisor(UUID identifier);
    void undoDeleteAdvisor(UUID identifier);
    PaginatedResponse<AdminAdvisorBasicResponse> adminSearchAdvisors(PaginationRequest paginationRequest, AdvisorSearchRequest request);
    PaginatedResponse<AdminAdvisorBasicResponse> getDeletedAdvisors(PaginationRequest paginationRequest);
}
