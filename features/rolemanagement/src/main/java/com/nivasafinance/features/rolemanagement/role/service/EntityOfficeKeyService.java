package com.nivasafinance.features.rolemanagement.role.service;

import com.nivasafinance.common.enums.EntityType;

import java.util.UUID;

/**
 * Service to extract office key from different entity types.
 * This allows the user assignment logic to work with any entity
 * that has an office, not just leads.
 */
public interface EntityOfficeKeyService {
    /**
     * Get office key for an entity based on entity type and identifier.
     * @param entityType Type of entity (LEAD, ADVISOR, etc.)
     * @param entityId Entity identifier
     * @return Office key, or null if entity not found or has no office
     */
    String getOfficeKey(EntityType entityType, UUID entityId);
    
    /**
     * Get office key for a lead.
     * @param leadId Lead identifier
     * @return Office key, or null if lead not found or has no office
     * @deprecated Use {@link #getOfficeKey(EntityType, UUID)} instead
     */
    @Deprecated
    String getOfficeKeyForLead(UUID leadId);
    
    /**
     * Get office key for an advisor.
     * @param advisorId Advisor identifier
     * @return Office key, or null if advisor not found or has no office
     * @deprecated Use {@link #getOfficeKey(EntityType, UUID)} instead
     */
    @Deprecated
    String getOfficeKeyForAdvisor(UUID advisorId);
}

