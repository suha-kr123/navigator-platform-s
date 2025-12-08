package com.nivasafinance.features.task.dto;

import com.nivasafinance.common.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Context information about the entity associated with a task.
 * Contains entity type, identifier, and additional entity-specific data in JSON format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityContextResponse {
    
    /**
     * The type of entity (e.g., LEAD)
     */
    private EntityType entityType;
    
    /**
     * The identifier of the entity
     */
    private UUID entityIdentifier;
    
    /**
     * Additional entity-specific data in JSON format.
     * For LEAD entities, this may contain:
     * - name: Lead primary person name
     * - phoneNumber: Lead primary person phone number
     * - status: Lead status
     * - Any other relevant lead information
     */
    private Map<String, Object> entityData;
}

