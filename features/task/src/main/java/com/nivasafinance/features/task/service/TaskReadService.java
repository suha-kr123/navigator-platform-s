package com.nivasafinance.features.task.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.task.dto.TaskResponse;

public interface TaskReadService {

    PaginatedResponse<TaskResponse> getTasksByAssignedTo(String assignedTo, boolean includeCompleted, PaginationRequest paginationRequest);

    // TODO: to be implemented by Disa S K after office management feature is implemented
    // PaginatedResponse<TaskResponse> getTasksByIds(List<Long> taskIds, boolean includeCompleted, PaginationRequest paginationRequest);
}
