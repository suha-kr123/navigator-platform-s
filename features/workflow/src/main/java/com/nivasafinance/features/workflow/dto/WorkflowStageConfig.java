package com.nivasafinance.features.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStageConfig {
    private String stageKey;
    private List<String> allowedAdhocTasks;
    private List<StageTaskConfig> stageTasks;
    /**
     * Default substage key to automatically set when transitioning to this stage.
     * If not specified, substage will be null.
     */
    private String defaultSubStage;
}

