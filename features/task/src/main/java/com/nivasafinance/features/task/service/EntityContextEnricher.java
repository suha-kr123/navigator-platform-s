package com.nivasafinance.features.task.service;

import com.nivasafinance.common.enums.EntityType;

import java.util.Map;
import java.util.UUID;

public interface EntityContextEnricher {
    
    EntityType getEntityType();
    
    Map<String, Object> enrichEntityData(UUID entityIdentifier);
}

