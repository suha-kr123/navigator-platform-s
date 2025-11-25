package com.nivasafinance.features.leadtasks.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadtasks.dto.LeadTaskResponse;

import java.util.List;
import java.util.UUID;

public interface LeadTaskReadService {
    
    PaginatedResponse<LeadTaskResponse> getTasksByLeadId(UUID leadIdentifier, PaginationRequest paginationRequest);
    
    List<String> getAvailableAdhocTasks(UUID leadIdentifier, String stageKey);
    
}

