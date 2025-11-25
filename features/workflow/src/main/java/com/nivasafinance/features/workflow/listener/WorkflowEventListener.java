package com.nivasafinance.features.workflow.listener;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.StageTransitionEventPayload;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener that handles workflow events and creates tasks for stage transitions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventListener {

    private final WorkflowOrchestratorService workflowOrchestratorService;

    /**
     * Handles STAGE_TRANSITIONED events asynchronously to create tasks for the new stage.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleSystemEvent(SystemEvent<?> event) {
        String eventType = event.getEventType();

        // Only handle STAGE_TRANSITIONED events
        if (!BusinessEvent.STAGE_TRANSITIONED.toString().equals(eventType)) {
            return;
        }

        if (!(event.getPayload() instanceof StageTransitionEventPayload)) {
            log.warn("Received non-StageTransitionEventPayload for STAGE_TRANSITIONED event: {}", eventType);
            return;
        }

        StageTransitionEventPayload payload = (StageTransitionEventPayload) event.getPayload();

        EntityType entityType = payload.getEntityType();
        Long entityId = payload.getEntityId();
        String fromStageKey = payload.getFromStageKey();
        String toStageKey = payload.getToStageKey();
        String assignedTo = payload.getAssignedTo();

        if (!ValidationUtils.isNonNull(entityType) || !ValidationUtils.isNonNull(entityId) 
                || !ValidationUtils.isNonNull(toStageKey)) {
            log.warn("Invalid StageTransitionEventPayload: entityType={}, entityId={}, toStageKey={}. Skipping task creation.",
                    entityType, entityId, toStageKey);
            return;
        }

        try {
            String workflowConfigKey = workflowOrchestratorService.getEntityWorkflowConfigKey(entityId, entityType);
            if (!ValidationUtils.isNonNull(workflowConfigKey)) {
                log.warn("Workflow config key not found for entityId: {}, entityType: {}. Skipping task creation.",
                        entityId, entityType);
                return;
            }

            // Build context - assignedTo can be null for initial stage
            // Due date will be calculated from task config's dueDateLogicExpression if not provided
            Map<String, Object> taskContext = new HashMap<>();
            if (ValidationUtils.isNonNull(assignedTo)) {
                taskContext.put(WorkflowConstants.ContextKeys.ASSIGNED_TO, assignedTo);
            }
            // Note: fromStageKey is already set to "landing" in the event payload for initial stage
            // Note: DUE_AT is intentionally not set here - it will be calculated from task config if needed

            workflowOrchestratorService.createTasksForStageTransition(
                    entityId,
                    entityType,
                    fromStageKey,
                    toStageKey,
                    assignedTo,
                    workflowConfigKey,
                    taskContext
            );
        } catch (Exception e) {
            log.error("Failed to create tasks for stage transition: entityId={}, entityType={}, fromStage={}, toStage={}",
                    entityId, entityType, fromStageKey, toStageKey, e);
        }
    }
}