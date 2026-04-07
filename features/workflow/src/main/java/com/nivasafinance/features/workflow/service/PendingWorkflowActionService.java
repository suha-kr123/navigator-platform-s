package com.nivasafinance.features.workflow.service;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.dto.PendingActionResponse;

import java.util.List;
import java.util.UUID;

public interface PendingWorkflowActionService {

    List<PendingActionResponse> getPendingActionsBySourceTask(UUID sourceTaskIdentifier);

    List<PendingActionResponse> getPendingActionsByEntity(UUID entityIdentifier, EntityType entityType);
}
