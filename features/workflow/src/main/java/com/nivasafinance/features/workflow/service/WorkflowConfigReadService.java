package com.nivasafinance.features.workflow.service;

import com.nivasafinance.features.workflow.entity.WorkflowConfig;

public interface WorkflowConfigReadService {

    WorkflowConfig getWorkflowConfigByKey(String workflowConfigKey);
}

