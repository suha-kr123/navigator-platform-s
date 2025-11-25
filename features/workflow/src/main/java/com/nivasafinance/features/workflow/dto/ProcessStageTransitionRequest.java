package com.nivasafinance.features.workflow.dto;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessStageTransitionRequest {
    
    private Long entityId;
    private EntityType entityType;
    private String fromStageKey;
    private String toStageKey;
    private String assignedTo;
    private String remarks;
    private WorkflowConfig workflowConfig;
    private Map<String, Object> context;
}

