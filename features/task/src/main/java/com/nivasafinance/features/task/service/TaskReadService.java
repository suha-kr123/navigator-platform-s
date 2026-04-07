package com.nivasafinance.features.task.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.dto.TaskResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskReadService {

    PaginatedResponse<TaskResponse> getTasksByAssignedTo(String assignedTo, boolean includeCompleted,
            LocalDate dueDateFrom, LocalDate dueDateTo, PaginationRequest paginationRequest);

    
    PaginatedResponse<TaskResponse> getAllTasks(EntityType entityType, UUID entityId, boolean includeCompleted, PaginationRequest paginationRequest);

    /**
     * All tasks for a lead (open and/or completed), ordered by due date. Used for dashboard task timeline.
     */
    List<TaskResponse> findAllTasksForLead(UUID leadIdentifier, boolean includeCompleted);
}
