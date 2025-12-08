package com.nivasafinance.features.lead.task;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.task.service.EntityContextEnricher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enricher for LEAD entity context.
 * Provides lead-specific data like name, phone number, status, etc.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeadEntityContextEnricher implements EntityContextEnricher {
    
    private final LeadReadService leadReadService;
    
    @Override
    public EntityType getEntityType() {
        return EntityType.LEAD;
    }
    
    @Override
    public Map<String, Object> enrichEntityData(UUID entityIdentifier) {
        try {
            if (entityIdentifier == null) {
                log.warn("Cannot enrich lead entity data: entityIdentifier is null");
                return new HashMap<>();
            }
            
            LeadResponse lead = leadReadService.getLeadByIdentifier(entityIdentifier);
            
            if (lead == null) {
                log.warn("Cannot enrich lead entity data: LeadResponse is null for identifier {}", entityIdentifier);
                return new HashMap<>();
            }
            
            Map<String, Object> entityData = new HashMap<>();
            entityData.put("name", lead.getPrimaryPersonName());
            entityData.put("phoneNumber", lead.getPrimaryPersonNumber());
            // Send stage key (name can be fetched by UI using stage key if needed)
            entityData.put("stageKey", lead.getCurrentStageKey());
            // Send substage key and display name for UI
            entityData.put("subStageKey", lead.getCurrentSubStageKey());
            entityData.put("subStageName", lead.getCurrentSubStageName());
            
            // Safely handle leadIdentifier - it might be null
            if (lead.getLeadIdentifier() != null) {
                entityData.put("leadIdentifier", lead.getLeadIdentifier().toString());
            } else {
                log.warn("Lead identifier is null in LeadResponse for entityIdentifier {}", entityIdentifier);
            }
            
            log.debug("Successfully enriched lead entity data for identifier {}", entityIdentifier);
            return entityData;
        } catch (com.nivasafinance.features.lead.exception.LeadNotFoundException e) {
            log.warn("Lead not found for identifier {}: {}", entityIdentifier, e.getMessage());
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Failed to enrich lead entity data for identifier {}: {}", entityIdentifier, e.getMessage(), e);
            return new HashMap<>();
        }
    }
}

