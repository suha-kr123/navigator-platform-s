package com.nivasafinance.features.workflow.service;

import java.util.List;

import com.nivasafinance.features.workflow.entity.WorkflowConfig;

public interface WorkflowConfigReadService {

    WorkflowConfig getWorkflowConfigByKey(String workflowConfigKey);
    
    boolean canCreateAdhocTask(String workflowConfigKey, String taskConfigKey, String stageKey);

    List<String> getAdhocTaskKeysForStage(String workflowConfigKey, String stageKey);

    String getStageIdentifier(String workflowConfigKey, String stageKey);

}

