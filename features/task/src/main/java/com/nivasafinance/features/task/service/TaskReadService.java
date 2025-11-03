package com.nivasafinance.features.task.service;

import java.util.List;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.task.dto.TaskResponse;

public interface TaskReadService {

    PaginatedResponse<TaskResponse> getTasksByAssignedTo(String assignedTo, boolean includeCompleted, PaginationRequest paginationRequest);

    PaginatedResponse<TaskResponse> getTasksByAssignedToRole(String assignedToRole, boolean includeCompleted, PaginationRequest paginationRequest);

    // todo: to be implemented by Disa S K after office management feature is implemented
    // PaginatedResponse<TaskResponse> getTasksByIds(List<Long> taskIds, boolean includeCompleted, PaginationRequest paginationRequest);
}
