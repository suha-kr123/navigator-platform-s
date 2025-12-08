package com.nivasafinance.features.workflow.orchestrator;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.dto.StageConfigResponse;

import java.util.List;
import java.util.Map;

public interface WorkflowOrchestratorService {
    
    /**
     * Creates tasks for a stage transition (called asynchronously by event handler).
     */
    void createTasksForStageTransition(
            Long entityId,
            EntityType entityType,
            String fromStageKey,
            String toStageKey,
            String assignedTo,
            String workflowConfigKey,
            Map<String, Object> context
    );

    Object createAdhocTaskForStage(Long entityId, EntityType entityType, String stageKey, String taskConfigKey);
    
    void validateStageTransition(Long entityId, EntityType entityType, String stageKey, String assignedTo, boolean hasExistingHistory);
    
    StageConfigResponse getStageConfig(String stageKey);
    
    List<String> getAdhocTaskKeysForStage(String workflowConfigKey, String stageKey);
    
    /**
     * Gets the default substage for a stage from the workflow configuration.
     * Returns null if no default substage is configured.
     */
    String getDefaultSubStageForStage(String workflowConfigKey, String stageKey);
    
    String getEntityWorkflowConfigKey(Long entityId, EntityType entityType);

}
