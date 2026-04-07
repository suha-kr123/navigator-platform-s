package com.nivasafinance.features.workflow.adapter;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;

import java.util.Map;
import java.util.UUID;


/**
 * Adapter interface for entity-specific workflow operations.
 * Each entity type (Lead, Application, etc.) provides its own implementation.
 */
public interface EntityWorkflowAdapter {
    
    /**
     * Returns the entity type this adapter handles.
     */
    EntityType getEntityType();
    
    /**
     * Gets the entity by ID.
     */
    Object getEntity(Long entityId);
    
    /**
     * Extracts the entity identifier (UUID) from the entity.
     */
    UUID getEntityIdentifier(Object entity);
    
    /**
     * Extracts preferred call window from the entity.
     */
    TaskDetailsRequest.PreferredCallWindow getPreferredCallWindow(Object entity);
    
    /**
     * Gets the workflow config key from the entity.
     */
    String getWorkflowConfigKey(Long entityId);
    
    /**
     * Creates a task and associates it with the entity (synchronous, no reflection).
     */
    Object createTaskAndAssociate(Long entityId, Object createTaskRequest, Map<String, Object> taskDetails);

    /**
     * Resolves the database ID (Long) from the entity's UUID identifier.
     */
    Long resolveEntityId(UUID entityIdentifier);

    /**
     * Transitions the entity from one stage to another.
     */
    void transitionStage(UUID entityIdentifier, String fromStageKey, String toStageKey, String assignTo);

    /**
     * Closes all open tasks for the entity with the given outcome.
     */
    void closeOpenTasks(UUID entityIdentifier, String outcome);

    /**
     * Changes the substage of the entity within its current stage.
     */
    void changeSubStage(UUID entityIdentifier, String stageKey, String subStageKey);
}

