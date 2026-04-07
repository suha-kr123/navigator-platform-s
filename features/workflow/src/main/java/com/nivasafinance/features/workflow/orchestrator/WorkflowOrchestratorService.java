package com.nivasafinance.features.workflow.orchestrator;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.dto.StageConfigResponse;

import java.util.Map;
import java.util.UUID;

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
    
    void validateStageTransition(Long entityId, EntityType entityType, String stageKey, String assignedTo, boolean hasExistingHistory);
    
    StageConfigResponse getStageConfig(String stageKey);
    
    /**
     * Gets the default substage for a stage from the workflow configuration.
     * Returns null if no default substage is configured.
     */
    String getDefaultSubStageForStage(String workflowConfigKey, String stageKey);
    
    String getEntityWorkflowConfigKey(Long entityId, EntityType entityType);

    /**
     * Processes post-task-completion actions by looking up workflow config,
     * calling BRE, and executing auto-execute actions immediately while
     * storing manual actions as PENDING for user confirmation.
     */
    void processTaskCompletion(
            UUID entityIdentifier,
            EntityType entityType,
            UUID sourceTaskIdentifier,
            String taskConfigKey,
            String outcome,
            String stageKey,
            String assignedTo
    );

    /**
     * Executes a single pending workflow action with user-provided input.
     */
    void executePendingAction(UUID actionIdentifier, String assignTo, java.time.LocalDateTime dueDate);

    /**
     * Cancels a single pending workflow action.
     */
    void cancelPendingAction(UUID actionIdentifier);

    /**
     * Cancels all pending workflow actions originating from a specific task completion.
     */
    void cancelPendingActionsBySourceTask(UUID sourceTaskIdentifier);
}
