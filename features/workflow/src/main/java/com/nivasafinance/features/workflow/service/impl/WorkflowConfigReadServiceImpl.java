package com.nivasafinance.features.workflow.service.impl;

import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkflowConfigReadServiceImpl implements WorkflowConfigReadService {

    private final WorkflowConfigRepositoryWrapper workflowConfigRepositoryWrapper;

    @Override
    public WorkflowConfig getWorkflowConfigByKey(String workflowConfigKey) {
        return workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(workflowConfigKey);
    }
}

