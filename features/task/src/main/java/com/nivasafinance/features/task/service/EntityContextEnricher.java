package com.nivasafinance.features.task.service;

import com.nivasafinance.common.enums.EntityType;

import java.util.Map;
import java.util.UUID;

/**
 * Interface for enriching entity context data for tasks.
 * Each entity type should implement this interface to provide its own enrichment logic.
 */
public interface EntityContextEnricher {
    
    /**
     * Returns the entity type this enricher handles.
     */
    EntityType getEntityType();
    
    /**
     * Enriches entity data for the given entity identifier.
     * 
     * @param entityIdentifier The identifier of the entity
     * @return Map containing entity-specific data (e.g., name, phoneNumber, status, etc.)
     */
    Map<String, Object> enrichEntityData(UUID entityIdentifier);
}

