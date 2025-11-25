package com.nivasafinance.features.workflow.adapter;

import com.nivasafinance.common.enums.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry for entity workflow adapters.
 * Automatically discovers and registers all EntityWorkflowAdapter implementations.
 */
@Component
@RequiredArgsConstructor
public class EntityWorkflowAdapterRegistry {
    
    private final List<EntityWorkflowAdapter> adapters;
    private Map<EntityType, EntityWorkflowAdapter> adapterMap;
    
    /**
     * Gets the adapter for the given entity type.
     * 
     * @param entityType The entity type to get adapter for
     * @return The adapter for the entity type
     * @throws IllegalArgumentException if no adapter is found for the entity type
     */
    public EntityWorkflowAdapter getAdapter(EntityType entityType) {
        if (adapterMap == null) {
            adapterMap = adapters.stream()
                    .collect(Collectors.toMap(
                            EntityWorkflowAdapter::getEntityType,
                            Function.identity()
                    ));
        }
        
        EntityWorkflowAdapter adapter = adapterMap.get(entityType);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter found for entity type: " + entityType);
        }
        return adapter;
    }
}

