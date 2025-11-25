package com.nivasafinance.features.workflow.repository;

import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.exception.WorkflowConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowConfigRepositoryWrapper {

    private final WorkflowConfigRepository repository;
    private final MessageSource messageSource;

    public WorkflowConfig findActiveByWorkflowConfigKey(String workflowConfigKey) {
        return repository.findByWorkflowConfigKey(workflowConfigKey)
                .orElseThrow(() -> WorkflowConfigNotFoundException.workflowConfigNotFound(workflowConfigKey, messageSource));
    }
}

