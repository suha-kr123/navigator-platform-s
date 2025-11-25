package com.nivasafinance.features.workflow.adapter;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;

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
     * Creates an adhoc task request for the entity type.
     */
    Object createAdhocTaskRequest(String taskConfigKey, String stageKey);
    
    /**
     * Creates a task and associates it with the entity (synchronous, no reflection).
     */
    Object createTaskAndAssociate(Long entityId, Object createTaskRequest, java.util.Map<String, Object> taskDetails);
    
    /**
     * Creates an adhoc task for the entity (synchronous, no reflection).
     */
    Object createAdhocTask(UUID entityIdentifier, Object createAdhocTaskRequest);
}

